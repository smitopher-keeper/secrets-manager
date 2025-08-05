package com.keepersecurity.spring.ksm.autoconfig;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

/**
 * Validates that a FIPS-certified crypto provider is active when IL5 enforcement is enabled.
 */
class Il5ComplianceValidator implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(Il5ComplianceValidator.class);

  private final KeeperKsmProperties properties;
  private final Environment environment;

  /**
   * Creates a new validator used to check IL5 compliance.
   *
   * @param properties configuration properties that include IL5 enforcement flags
   * @param environment Spring environment used to look up override properties
   */
  Il5ComplianceValidator(KeeperKsmProperties properties, Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

  /**
   * Verifies IL5 compliance after all properties are set. When enforcement is enabled, this
   * method ensures that a FIPS-certified crypto provider is present and that audit logging is
   * enabled at the INFO level or higher with a secure sink. Checks can be downgraded to warnings by
   * setting the {@code crypto.check.mode} or {@code audit.check.mode} environment properties to
   * {@code warn}.
   *
   * @throws IllegalStateException if any IL5 requirement is not satisfied and the corresponding
   *     check mode is not set to {@code warn}
   */
  @Override
  public void afterPropertiesSet() {
    if (!properties.isEnforceIl5()) {
      return;
    }
    boolean fipsPresent = Arrays.stream(Security.getProviders())
        .map(Provider::getName)
        .map(String::toUpperCase)
        .anyMatch(name -> name.contains("FIPS"));
    if (!fipsPresent) {
      String message = "No FIPS-certified crypto provider (e.g. BCFIPS) is active. IL5 mode requires FIPS-compliant crypto.";
      if ("warn".equalsIgnoreCase(environment.getProperty("crypto.check.mode"))) {
        LOGGER.atWarn().log(message);
      } else {
        LOGGER.atError().log(message);
        throw new IllegalStateException(message);
      }
    }

    Logger baseLogger = LoggerFactory.getLogger("com.keepersecurity");
    Logger ksmLogger = LoggerFactory.getLogger("com.keepersecurity.ksm");
    String auditMode = environment.getProperty("audit.check.mode");
    boolean warn = "warn".equalsIgnoreCase(auditMode);

    boolean levelOk = isLevelInfoOrHigher(baseLogger) || isLevelInfoOrHigher(ksmLogger);
    if (!levelOk) {
      String message =
          "Audit logger level for 'com.keepersecurity' or 'com.keepersecurity.ksm' must be set to INFO or higher.";
      if (warn) {
        LOGGER.atWarn().log(message);
      } else {
        LOGGER.atError().log(message);
        throw new IllegalStateException(message);
      }
    }

    Logger rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    boolean sinkPresent = hasSecureAppender(baseLogger) || hasSecureAppender(ksmLogger)
        || hasSecureAppender(rootLogger);
    if (!sinkPresent) {
      String message =
          "Audit logging not detected. IL5 enforcement requires audit visibility.";
      if (warn) {
        LOGGER.atWarn().log(message);
      } else {
        LOGGER.atError().log(message);
        throw new IllegalStateException(message);
      }
    }
  }

  private boolean isLevelInfoOrHigher(Logger logger) {
    if (logger instanceof ch.qos.logback.classic.Logger logback) {
      return logback.getEffectiveLevel().isGreaterOrEqual(Level.INFO);
    }
    return logger.isInfoEnabled() || logger.isWarnEnabled() || logger.isErrorEnabled();
  }

  private boolean hasSecureAppender(Logger logger) {
    if (logger instanceof ch.qos.logback.classic.Logger logback) {
      for (java.util.Iterator<Appender<ILoggingEvent>> it = logback.iteratorForAppenders(); it.hasNext(); ) {
        Appender<ILoggingEvent> appender = it.next();
        if (appender instanceof FileAppender) {
          return true;
        }
      }
    }
    return false;
  }
}

