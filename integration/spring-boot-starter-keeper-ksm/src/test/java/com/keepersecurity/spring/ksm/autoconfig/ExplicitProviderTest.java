package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json",
        "keeper.ksm.provider=org.bouncycastle.jce.provider.BouncyCastleProvider"
})
class ExplicitProviderTest {

    @AfterEach
    void cleanup() {
        Security.removeProvider("BCFIPS");
        Security.removeProvider("BC");
    }

    @Test
    void bcProviderSelected() {
        assertNotNull(Security.getProvider("BC"), "Bouncy Castle provider should be registered");
    }
}
