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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import javax.crypto.spec.SecretKeySpec;
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
 * Spring Boot auto-configuration for Keeper Secrets Manager.
 * <p>
 * This configuration class registers a {@link SecretsManagerOptions} bean based on
 * {@link KeeperKsmProperties}. It supports initialisation from a one-time token
 * as well as loading an existing JSON configuration file.
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

  // package-private constructor required for Spring Boot
  KeeperKsmAutoConfiguration() {
  }

  /**
   * Creates a {@link SecretsManagerOptions} bean based on the supplied
   * {@link KeeperKsmProperties}. If a one-time token is configured it will be
   * consumed to initialise the local configuration before the application exits.
   * Otherwise the existing configuration is loaded from the configured location.
   *
   * @param properties bound Keeper configuration properties
   * @return a fully configured {@link SecretsManagerOptions} instance
   */
  @Bean
  @ConditionalOnMissingBean // Only create the bean if one isn't already defined in the context
  SecretsManagerOptions secretsManagerOptions(KeeperKsmProperties properties) {
    Optional<Path> tokenPath = Optional.ofNullable(properties.getOneTimeToken());
    tokenPath.ifPresent(path -> {
      String token;
      try {
        token = Files.readString(path);
      } catch (IOException e) {
        String message = "failure loading KMS One Time Token";
        LOGGER.atError().setCause(e).log(message);
        throw new IllegalStateException(message, e);
      }
      consumeToken(token, path, properties);
    });
    KeyValueStorage storage = new InMemoryStorage(getKmsConfig(properties));
    return new SecretsManagerOptions(storage);
  }

  private String getKmsConfig(KeeperKsmProperties properties) {
    try {
      return Files.readString(properties.getSecretPath());
    } catch (IOException e) {
      String message = "failure loading KMS Config";
      LOGGER.atError().setCause(e).log(message);
      throw new IllegalStateException(message, e);
    }
  }

  private void consumeToken(String token, Path tokenFile, KeeperKsmProperties props) {
    InMemoryStorage inMemoryStorage = new InMemoryStorage();
    SecretsManager.initializeStorage(inMemoryStorage, token);
    SecretsManagerOptions options = new SecretsManagerOptions(inMemoryStorage);
    // performing a get consumes the one time token
    SecretsManager.getSecrets(options, List.of("AAAAAAAAAAAAAAAAAAAAAA"));

    ObjectNode config = new ObjectMapper().createObjectNode();
    CONFIG_KEYS.forEach(key -> config.put(key, inMemoryStorage.getString(key)));
    var providerType = props.getProviderType();
    switch (providerType) {
      case RAW -> saveRawConfigToFile(config, props);
      case DEFAULT, NAMED, BC_FIPS, ORACLE_FIPS -> saveConfigToKeystore(config, props);
      case AWS -> AwsSaver.save(config, props);
      case AZURE -> AzureSaver.save(config, props);
      case GOOGLE -> GoogleSaver.save(config, props);
      default -> throw new IllegalArgumentException("Unexpected or unimplemented provider: " + providerType);
    }

    try {
      Files.deleteIfExists(tokenFile);
    } catch (IOException e) {
      LOGGER.atWarn().setCause(e).log("Failed to delete one-time token file {}", tokenFile);
    }

    String message = "One-time token consumed. Remove the property 'keeper.ksm.one-time-token' and restart the application.";
    LOGGER.atInfo().log(message);
    throw new OneTimeTokenConsumedException(message);
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

  private String resolveKeystoreType(KsmConfigProvider providerType) {
    return switch (providerType) {
      case BC_FIPS -> "BCFKS";
      case ORACLE_FIPS -> "PKCS12";
      case DEFAULT, NAMED -> {
        String ext = KsmKeystoreDefaults.resolveDefaultExtension().toLowerCase();
        yield switch (ext) {
          case "p12", "pfx" -> "PKCS12";
          case "bcfks" -> "BCFKS";
          case "bks" -> "BKS";
          case "jks" -> "JKS";
          default -> {
            String message = "Unsupported keystore extension: " + ext;
            LOGGER.atWarn().log(message);
            throw new IllegalArgumentException(message);
          }
        };
      }
      default -> {
        String message = "Unsupported provider for keystore persistence: " + providerType;
        LOGGER.atWarn().log(message);
        throw new IllegalArgumentException(message);
      }
    };
  }

  private void saveConfigToKeystore(ObjectNode config, KeeperKsmProperties props) {
    try {
      Path keystorePath = props.getSecretPath();
      Files.createDirectories(keystorePath.getParent());

      Class<? extends Provider> providerClass = props.getProviderClass();
      if (providerClass != null) {
        Provider provider = providerClass.getDeclaredConstructor().newInstance();
        Security.addProvider(provider);
      }

      char[] password = props.getSecretPassword().toCharArray();
      String keystoreType = resolveKeystoreType(props.getProviderType());
      KeyStore ks = KeyStore.getInstance(keystoreType);
      if (Files.exists(keystorePath)) {
        try (InputStream in = Files.newInputStream(keystorePath)) {
          ks.load(in, password);
        }
      } else {
        ks.load(null, null);
      }

      SecretKeySpec secret = new SecretKeySpec(config.toString().getBytes(StandardCharsets.UTF_8), "RAW");
      KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secret);
      KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(password);
      ks.setEntry(props.getSecretUser(), entry, protection);

      try (OutputStream out = Files.newOutputStream(keystorePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
        ks.store(out, password);
      }
    } catch (IOException | GeneralSecurityException | ReflectiveOperationException e) {
      String message = "Failed to persist the KMS Config keystore";
      LOGGER.atError().setCause(e).log(message);
      throw new IllegalStateException(message, e);
    }
  }


  private static class AwsSaver {
    static void save(ObjectNode config, KeeperKsmProperties props) {
      requireClass("software.amazon.awssdk.services.secretsmanager.SecretsManagerClient");
      try {
        software.amazon.awssdk.services.secretsmanager.SecretsManagerClient client =
            software.amazon.awssdk.services.secretsmanager.SecretsManagerClient.builder().build();
        String id = props.getSecretPath().toString();
        try {
          client.createSecret(software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest
              .builder()
              .name(id)
              .secretString(config.toString())
              .build());
        } catch (software.amazon.awssdk.services.secretsmanager.model.ResourceExistsException e) {
          client.putSecretValue(software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest
              .builder()
              .secretId(id)
              .secretString(config.toString())
              .build());
        }
      } catch (Exception e) {
        throw new IllegalStateException("Failed to persist the KMS Config to AWS Secrets Manager", e);
      }
    }
  }

  private static class AzureSaver {
    static void save(ObjectNode config, KeeperKsmProperties props) {
      requireClass("com.azure.security.keyvault.secrets.SecretClient");
      try {
        com.azure.security.keyvault.secrets.SecretClient client =
            new com.azure.security.keyvault.secrets.SecretClientBuilder()
                .vaultUrl(props.getSecretPath().toString())
                .credential(new com.azure.identity.DefaultAzureCredentialBuilder().build())
                .buildClient();
        client.setSecret(props.getSecretUser(), config.toString());
      } catch (Exception e) {
        throw new IllegalStateException("Failed to persist the KMS Config to Azure Key Vault", e);
      }
    }
  }

  private static class GoogleSaver {
    static void save(ObjectNode config, KeeperKsmProperties props) {
      requireClass("com.google.cloud.secretmanager.v1.SecretManagerServiceClient");
      try (com.google.cloud.secretmanager.v1.SecretManagerServiceClient client =
          com.google.cloud.secretmanager.v1.SecretManagerServiceClient.create()) {
        String secretId = props.getSecretPath().toString();
        com.google.cloud.secretmanager.v1.SecretName name =
            com.google.cloud.secretmanager.v1.SecretName.parse(secretId);
        try {
          client.createSecret(
              com.google.cloud.secretmanager.v1.CreateSecretRequest.newBuilder()
                  .setParent(name.getProject())
                  .setSecretId(name.getSecret())
                  .setSecret(com.google.cloud.secretmanager.v1.Secret.newBuilder().build())
                  .build());
        } catch (com.google.api.gax.rpc.AlreadyExistsException ignore) {
          // secret already exists
        }
        com.google.cloud.secretmanager.v1.SecretPayload payload =
            com.google.cloud.secretmanager.v1.SecretPayload.newBuilder()
                .setData(com.google.protobuf.ByteString.copyFromUtf8(config.toString()))
                .build();
        client.addSecretVersion(
            com.google.cloud.secretmanager.v1.AddSecretVersionRequest.newBuilder()
                .setParent(name.toString())
                .setPayload(payload)
                .build());
      } catch (Exception e) {
        throw new IllegalStateException("Failed to persist the KMS Config to Google Secret Manager", e);
      }
    }
  }

  private static void requireClass(String fqcn) {
    try {
      Class.forName(fqcn);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(fqcn + " not found on the classpath");
    }
  }
}
