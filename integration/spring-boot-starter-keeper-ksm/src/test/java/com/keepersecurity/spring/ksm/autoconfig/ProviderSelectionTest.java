package com.keepersecurity.spring.ksm.autoconfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KeeperKsmAutoConfiguration.class,
    properties = {"keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json",
        "keeper.ksm.provider-class=com.keepersecurity.spring.ksm.autoconfig.TestBcProvider"})
class ProviderSelectionTest {

  @AfterEach
  void cleanup() {
    Security.removeProvider("BC");
  }

  @Test
  void bcProviderSelected() {
    Security.addProvider(new TestBcProvider());
    assertNotNull(Security.getProvider("BC"), "BC provider should be registered");
  }

}
