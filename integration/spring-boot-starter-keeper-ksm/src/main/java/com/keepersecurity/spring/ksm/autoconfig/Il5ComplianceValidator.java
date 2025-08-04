package com.keepersecurity.spring.ksm.autoconfig;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

/**
 * Validates that a FIPS-certified crypto provider is active when IL5 enforcement is enabled.
 */
class Il5ComplianceValidator implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(Il5ComplianceValidator.class);

  private final KeeperKsmProperties properties;
  private final Environment environment;

  Il5ComplianceValidator(KeeperKsmProperties properties, Environment environment) {
    this.properties = properties;
    this.environment = environment;
  }

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
  }
}

