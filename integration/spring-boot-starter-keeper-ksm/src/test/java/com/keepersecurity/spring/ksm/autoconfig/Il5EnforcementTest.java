package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Il5EnforcementTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KeeperKsmAutoConfiguration.class)
            .withPropertyValues("keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json");

    @Test
    void failsWithoutFipsProviderWhenIl5Enforced() {
        contextRunner
                .withPropertyValues(
                        "keeper.ksm.provider=org.bouncycastle.jce.provider.BouncyCastleProvider",
                        "keeper.ksm.enforce-il5=true")
                .run(context -> org.assertj.core.api.Assertions.assertThat(context).hasFailed());
    }
}
