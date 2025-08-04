package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Il5EnforcementTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KeeperKsmAutoConfiguration.class)
            .withPropertyValues("keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json");

    @Test
    void failsWithoutHsmProviderWhenIl5Enforced() {
        contextRunner
                .withPropertyValues(
                        "keeper.ksm.enforce-il5=true",
                        "keeper.ksm.container-type=sun_pkcs11")
                .run(context -> org.assertj.core.api.Assertions.assertThat(context).hasFailed());
    }

    @Test
    void softHsm2NotAllowedWithIl5Enforcement() {
        contextRunner
                .withPropertyValues(
                        "keeper.ksm.enforce-il5=true",
                        "keeper.ksm.container-type=sun_pkcs11",
                        "keeper.ksm.hsm-provider=softHsm2")
                .run(context -> org.assertj.core.api.Assertions.assertThat(context).hasFailed());
    }

    @Test
    void awsCloudHsmAllowedWithIl5Enforcement() {
        contextRunner
                .withPropertyValues(
                        "keeper.ksm.enforce-il5=true",
                        "keeper.ksm.container-type=sun_pkcs11",
                        "keeper.ksm.hsm-provider=awsCloudHsm")
                .run(context -> org.assertj.core.api.Assertions.assertThat(context).hasNotFailed());
    }
}
