package com.keepersecurity.spring.ksm.autoconfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import java.security.Security;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
    classes = {KeeperKsmAutoConfiguration.class, ProviderSelectionTest.Config.class},
    properties = {
      "keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json",
      "keeper.ksm.provider-class=com.keepersecurity.spring.ksm.autoconfig.TestBcProvider"
    })
class ProviderSelectionTest {

  static class Config {
    @Bean
    KeyValueStorage ksmConfig() {
      return new InMemoryStorage("{}");
    }
  }

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
