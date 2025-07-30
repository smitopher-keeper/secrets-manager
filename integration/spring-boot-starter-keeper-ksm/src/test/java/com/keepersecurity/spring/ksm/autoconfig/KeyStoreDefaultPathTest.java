package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import com.keepersecurity.spring.ksm.autoconfig.KeeperKsmProperties;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "keeper.ksm.container-type=keystore",
        "keeper.ksm.secret-user=ksm-config",
        "keeper.ksm.secret-password=changeme"
})
class KeyStoreDefaultPathTest {

    @Autowired
    private SecretsManagerOptions options;

    @Autowired
    private KeeperKsmProperties properties;

    @Test
    void defaultPathUsesBcksExtensionWhenBouncyAvailable() {
        String home = System.getProperty("user.home");
        String expected = Paths.get(home, ".keeper", "ksm", "ksm-config.bcks").toString();
        assertThat(properties.getSecretPath()).isEqualTo(expected);
    }
}
