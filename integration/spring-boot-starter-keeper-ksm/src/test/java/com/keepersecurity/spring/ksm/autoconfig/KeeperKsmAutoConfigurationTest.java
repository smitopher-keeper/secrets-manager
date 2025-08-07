package com.keepersecurity.spring.ksm.autoconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
    classes = {KeeperKsmAutoConfiguration.class, KeeperKsmAutoConfigurationTest.Config.class})
class KeeperKsmAutoConfigurationTest {

  static class Config {
    @Bean
    KeyValueStorage ksmConfig() throws IOException {
      String json = Files.readString(Path.of("src/test/resources/starter-ksm-config.json"));
      return new InMemoryStorage(json);
    }
  }

  @Autowired private SecretsManagerOptions options;

  @Test
  void contextLoads() {
    assertNotNull(options, "SecretsManagerOptions bean should be initialized");
    String clientId = options.getStorage().getString("clientId");
    assertEquals("client-id", clientId, "clientId should be loaded from config file");
  }
}
