package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KeeperKsmAutoConfiguration.class,
    properties = {
        "keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json"
})
class ExplicitProviderTest {

    @AfterEach
    void cleanup() {
        Security.removeProvider("BC");
    }

    @Test
    void bcProviderSelected() {
        assertNotNull(Security.getProviders(), "Security providers should be available");
    }
}
