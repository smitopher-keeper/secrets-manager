package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {"keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json"})
class ProviderSelectionTest {

    @AfterEach
    void cleanup() {
        Security.removeProvider("BCFIPS");
        Security.removeProvider("BC");
    }

    @Test
    void fipsProviderLoadedByDefault() {
        assertNotNull(Security.getProvider("BCFIPS"), "FIPS provider should be registered");
    }
}
