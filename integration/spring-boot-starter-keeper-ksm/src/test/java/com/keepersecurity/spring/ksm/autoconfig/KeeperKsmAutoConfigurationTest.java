package com.keepersecurity.spring.ksm.autoconfig;

import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KeeperKsmAutoConfiguration.class,
    properties = {"keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json"})
class KeeperKsmAutoConfigurationTest {

    @Autowired
    private SecretsManagerOptions options;

    @Test
    void contextLoads() {
        assertNotNull(options, "SecretsManagerOptions bean should be initialized");
        String clientId = options.getStorage().getString("clientId");
        assertEquals("client-id", clientId, "clientId should be loaded from config file");
    }
}
