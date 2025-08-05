package com.keepersecurity.spring.ksm.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Provider;
import java.security.Security;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Il5ComplianceValidatorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KeeperKsmAutoConfiguration.class)
            .withPropertyValues(
                    "keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json",
                    "keeper.ksm.container-type=sun_pkcs11",
                    "keeper.ksm.hsm-provider=awsCloudHsm",
                    "audit.check.mode=warn");

    @Test
    void failsWithoutFipsProviderWhenIl5Enforced() {
        contextRunner
                .withPropertyValues("keeper.ksm.enforce-il5=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void warnsInsteadOfFailingWhenModeWarn() {
        contextRunner
                .withPropertyValues(
                        "keeper.ksm.enforce-il5=true",
                        "crypto.check.mode=warn")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void passesWhenFipsProviderActive() {
        try {
            Security.addProvider(new TestFipsProvider());
            contextRunner
                    .withPropertyValues("keeper.ksm.enforce-il5=true")
                    .run(context -> assertThat(context).hasNotFailed());
        } finally {
            Security.removeProvider("BCFIPS");
        }
    }

    static class TestFipsProvider extends Provider {
        TestFipsProvider() {
            super("BCFIPS", 1.0, "test fips provider");
        }
    }
}

