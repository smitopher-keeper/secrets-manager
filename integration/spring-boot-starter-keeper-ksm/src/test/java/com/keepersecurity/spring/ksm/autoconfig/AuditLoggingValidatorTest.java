package com.keepersecurity.spring.ksm.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AuditLoggingValidatorTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(KeeperKsmAutoConfiguration.class)
          .withBean("ksmConfig", KeyValueStorage.class, () -> new InMemoryStorage("{}"))
          .withPropertyValues(
              "keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json",
              "keeper.ksm.provider-type=bc_fips",
              "crypto.check.mode=warn");

  @Test
  void failsWithoutAuditSinkWhenIl5Enforced() {
    contextRunner
        .withPropertyValues("keeper.ksm.enforce-il5=true")
        .run(context -> assertThat(context).hasFailed());
  }

  @Test
  void warnsInsteadOfFailingWhenAuditModeWarn() {
    contextRunner
        .withPropertyValues("keeper.ksm.enforce-il5=true", "audit.check.mode=warn")
        .run(context -> assertThat(context).hasNotFailed());
  }

  @Test
  void passesWhenAuditLoggerHasFileSink() {
    configureLogging("logback-audit.xml");
    contextRunner
        .withPropertyValues("keeper.ksm.enforce-il5=true")
        .run(context -> assertThat(context).hasNotFailed());
  }

  @Test
  void failsWhenAuditLoggerBelowInfo() {
    configureLogging("logback-audit-debug.xml");
    contextRunner
        .withPropertyValues("keeper.ksm.enforce-il5=true")
        .run(context -> assertThat(context).hasFailed());
  }

  private void configureLogging(String resource) {
    LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
    context.reset();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(context);
    try {
      configurator.doConfigure(
          Objects.requireNonNull(
              Thread.currentThread().getContextClassLoader().getResource(resource)));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
