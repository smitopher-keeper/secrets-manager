package com.keepersecurity.spring.ksm.autoconfig;

import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_APP_KEY;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_CLIENT_ID;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_CLIENT_KEY;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_HOSTNAME;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_OWNER_PUBLIC_KEY;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_PRIVATE_KEY;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_PUBLIC_KEY;
import static com.keepersecurity.secretsManager.core.SecretsManager.KEY_SERVER_PUBIC_KEY_ID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import com.keepersecurity.secretsManager.core.SecretsManager;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;

/**
 * Auto-configuration for Keeper Secrets Manager integration.
 * <p>
 * Creates a {@link SecretsManagerOptions} bean based on application properties for Keeper Secrets
 * Manager. Supports initialization from a one-time token or loading from an existing JSON config
 * file.
 */
@Configuration // Marks this class as a configuration source for Spring
@ConditionalOnClass(SecretsManager.class) // Only activate if the Keeper SDK is on the classpath
@EnableConfigurationProperties(KeeperKsmProperties.class) // Enable binding of KeeperKsmProperties
public class KeeperKsmAutoConfiguration {

  private static final List<String> CONFIG_KEYS;
  private static final Logger LOGGER = LoggerFactory.getLogger(KeeperKsmAutoConfiguration.class);

  static {
    CONFIG_KEYS = List.of(KEY_HOSTNAME,
        KEY_CLIENT_ID,
        KEY_PRIVATE_KEY,
        KEY_CLIENT_KEY,
        KEY_APP_KEY,
        KEY_OWNER_PUBLIC_KEY,
        KEY_PUBLIC_KEY,
        KEY_SERVER_PUBIC_KEY_ID);
  }

  /**
   * Configures the SecretsManagerOptions bean using KeeperKsmProperties. If a one-time token is
   * provided, it will initialize the local config storage (and create the config file). If a config
   * file path is provided, it will load credentials from that file.
   *
   * @param properties the Keeper KSM properties (bound from application configuration)
   * @return a SecretsManagerOptions instance configured for Keeper Secrets Manager access
   */
  @Bean
  @ConditionalOnMissingBean // Only create the bean if one isn't already defined in the context
  SecretsManagerOptions secretsManagerOptions(KeeperKsmProperties properties) {
    Optional<Path> tokenPath = Optional.ofNullable(properties.getOneTimeToken());
    tokenPath.ifPresent(path -> {
      String token;
      try {
        token = Files.readString(path);
        Files.delete(path);
      } catch (IOException e) {
        String message = "failure loading KMS One Tome Token";
        LOGGER.atError().setCause(e).log(message);
        throw new IllegalStateException(message, e);
      }
      consumeToken(token, properties);
    });
    KeyValueStorage storage = new InMemoryStorage(getKmsConfig(properties));
    return new SecretsManagerOptions(storage);
  }

  private String getKmsConfig(KeeperKsmProperties properties) {
    // TODO Auto-generated method stub
    return null;
  }

  private void consumeToken(String token, KeeperKsmProperties props) {
    InMemoryStorage inMemoryStorage = new InMemoryStorage();
    SecretsManager.initializeStorage(inMemoryStorage, token);
    SecretsManagerOptions options = new SecretsManagerOptions(inMemoryStorage);
    // performing a get consumes the one time token
    SecretsManager.getSecrets(options, List.of(UUID.randomUUID().toString()));

    ObjectNode config = new ObjectMapper().createObjectNode();
    CONFIG_KEYS.forEach(key -> config.put(key, inMemoryStorage.getString(key)));
    var providerType = props.getProviderType();
    switch (providerType) {
      case RAW -> saveRawConfigToFile(config, props);
      default -> throw new IllegalArgumentException("Unexpected value: " + providerType);
    }
  }

  private void saveRawConfigToFile(ObjectNode config, KeeperKsmProperties props) {
    Path parent = props.getSecretPath().getParent();
    try {
      Files.createDirectories(parent);
      Files.writeString(props.getSecretPath(), config.toString(), StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      String message = "Failed to persist the RAW KMS Config";
      LOGGER.atError().setCause(e).log(message);
      throw new IllegalStateException(message, e);
    }
  }

  private void saveConfigToKeystore(ObjectNode config, KeeperKsmProperties props) {
    // TODO Auto-generated method stub
  }

  private void saveConfigToCloud(ObjectNode config, KeeperKsmProperties props) {
    // TODO Auto-generated method stub
  }


}

