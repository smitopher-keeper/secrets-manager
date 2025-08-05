package com.keepersecurity.spring.ksm.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import java.util.Objects;

class AuditLoggingValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KeeperKsmAutoConfiguration.class)
            .withPropertyValues(
                    "keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json",
                    "keeper.ksm.container-type=sun_pkcs11",
                    "keeper.ksm.hsm-provider=awsCloudHsm",
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
                .withPropertyValues(
                        "keeper.ksm.enforce-il5=true",
                        "audit.check.mode=warn")
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
            configurator.doConfigure(Objects.requireNonNull(
                    Thread.currentThread().getContextClassLoader().getResource(resource)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
