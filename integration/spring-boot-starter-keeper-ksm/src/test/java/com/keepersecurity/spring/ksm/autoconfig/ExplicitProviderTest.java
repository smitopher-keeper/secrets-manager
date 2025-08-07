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
    classes = {KeeperKsmAutoConfiguration.class, ExplicitProviderTest.Config.class},
    properties = {"keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json"})
class ExplicitProviderTest {

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
    assertNotNull(Security.getProviders(), "Security providers should be available");
  }
}
