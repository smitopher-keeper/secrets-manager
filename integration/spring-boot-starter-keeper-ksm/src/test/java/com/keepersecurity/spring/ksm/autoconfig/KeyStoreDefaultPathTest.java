package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import com.keepersecurity.spring.ksm.autoconfig.KeeperKsmProperties;
import com.keepersecurity.spring.ksm.autoconfig.KsmKeystoreDefaults;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = KeeperKsmAutoConfiguration.class,
    properties = {
        "keeper.ksm.container-type=keystore",
        "keeper.ksm.secret-user=ksm-config",
        "keeper.ksm.secret-password=changeme"
})
@Disabled("Keystore path resolution depends on environment keystore provider")
class KeyStoreDefaultPathTest {

    @Autowired
    private SecretsManagerOptions options;

    @Autowired
    private KeeperKsmProperties properties;

    @Test
    void defaultPathUsesBcksExtensionWhenBouncyAvailable() {
        String home = System.getProperty("user.home");
        String extension = KsmKeystoreDefaults.resolveDefaultExtension();
        String expected = Paths.get(home, ".keeper", "ksm", "ksm-config." + extension).toString();
        assertThat(properties.getSecretPath()).isEqualTo(expected);
    }
}
