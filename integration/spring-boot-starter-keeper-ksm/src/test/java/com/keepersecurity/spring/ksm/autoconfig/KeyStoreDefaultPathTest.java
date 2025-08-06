package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.keepersecurity.spring.ksm.autoconfig.KeeperKsmProperties;
import com.keepersecurity.spring.ksm.autoconfig.KsmKeystoreDefaults;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {KeeperKsmAutoConfiguration.class, KeyStoreDefaultPathTest.MockOptionsConfig.class},
    properties = {
        "keeper.ksm.provider-type=default",
        "keeper.ksm.secret-user=ksm-config",
        "keeper.ksm.secret-password=changeme",
        "spring.main.allow-bean-definition-overriding=true"
})
class KeyStoreDefaultPathTest {

    private static final MockedStatic<KsmKeystoreDefaults> DEFAULTS = Mockito.mockStatic(KsmKeystoreDefaults.class);
    static {
        DEFAULTS.when(KsmKeystoreDefaults::resolveDefaultExtension).thenReturn("p12");
        DEFAULTS.when(KsmKeystoreDefaults::getDefaultKeystoreFilename).thenCallRealMethod();
    }

    @AfterAll
    static void cleanup() {
        DEFAULTS.close();
    }

    @Autowired
    private KeeperKsmProperties properties;

    @Test
    void defaultPathUsesMockedExtension() {
        String extension = KsmKeystoreDefaults.resolveDefaultExtension();
        Path expected = Paths.get("ksm-config." + extension);
        assertThat(properties.getSecretPath()).isEqualTo(expected);
    }

    @TestConfiguration
    static class MockOptionsConfig {
        @Bean
        SecretsManagerOptions secretsManagerOptions() {
            return Mockito.mock(SecretsManagerOptions.class);
        }
    }
}
