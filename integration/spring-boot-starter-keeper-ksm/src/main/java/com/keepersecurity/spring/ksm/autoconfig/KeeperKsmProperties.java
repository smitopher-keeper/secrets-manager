package com.keepersecurity.spring.ksm.autoconfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holder for properties in the {@code keeper.ksm.*} namespace.
 * <p>
 * The values are bound from application configuration files so that the
 * {@link KeeperKsmAutoConfiguration} can configure the SDK appropriately.
 */
@ConfigurationProperties(prefix = "keeper.ksm")
public class KeeperKsmProperties implements InitializingBean{

  private static final Logger LOGGER = LoggerFactory.getLogger(KeeperKsmProperties.class);

  // package-private constructor required for configuration binding
  KeeperKsmProperties() {
  }

  /**
   * Path to the secret container that holds the Keeper Secrets Manager configuration JSON. This can
   * be a file path or a location inside a secrets service.
   * <p>
   * If a one-time token is provided and this path is set, the token will be redeemed and the config
   * stored at this location. If not set and {@link KsmConfigProvider#RAW RAW} is used with a token,
   * a default file {@code "ksm-config.json"} will be used for the secretPath property.
   */
  private Path secretPath;
  /**
   * Path to a file containing a One-Time Access Token for Keeper Secrets Manager initialization.
   * <p>
   * The starter reads the token from this file, redeems it to generate the configuration and then deletes the file.
   * After a successful run the application terminates and you should remove this property from your configuration.
   */
  private Path oneTimeToken;

  /**
   * Fully qualified class name of a JCA security provider to register. Optional. If not set, a
   * provider will be chosen automatically.
   */
  private Class<? extends Provider> providerClass;

  /**
   * Type of secret container to use. Supported values correspond to the
   * constants in {@link KsmConfigProvider} such as
   * {@code default}, {@code named}, {@code bc_fips},
   * {@code oracle_fips}, {@code sun_pkcs11}, {@code softhsm2}, {@code aws},
   * {@code azure}, {@code aws_hsm}, {@code azure_hsm},
   * {@code google}, {@code fortanix}, {@code raw}, {@code hsm}.
   * For raw JSON configuration, use {@link KsmConfigProvider#RAW}.
   * Defaults to {@link KsmConfigProvider#DEFAULT}.
   */
  private KsmConfigProvider providerType = KsmConfigProvider.DEFAULT;

  /**
   * User name for accessing the secret container. Defaults to "changeme".
   */
  private String secretUser = "changeme";

  /**
   * Password for accessing the secret container. Defaults to "changeme".
   */
  private String secretPassword = "changeme";

  /**
   * Path to the PKCS#11 library when using the "pkcs11" container type.
   */
  private String pkcs11Library;

  /**
   * When true, the application will fail to start unless the IL-5 certified provider is
   * available. This can be used to enforce IL-5 compliance.
   */
  private boolean enforceIl5 = false;

  /**
   * List of Keeper record identifiers to load and expose as configuration
   * properties. Each entry may be a record UID or a string in the form
   * {@code "folder/record"} which will be resolved to the record UID at
   * runtime.
   */
  private List<String> records = new ArrayList<>();

  // Getters and setters for the properties

  /**
   * Returns the location where the KSM configuration is stored.
   *
   * @return path or URI of the configuration container
   */
  public Path getSecretPath() {
    return secretPath;
  }

  /**
   * Sets the location where the KSM configuration will be stored or loaded.
   *
   * @param secretPath path or URI to the configuration container
   */
  public void setSecretPath(Path secretPath) {
    this.secretPath = secretPath;
  }

  /**
   * Returns the path to the one-time token file used for initialization.
   *
   * @return path to the token file
   */
  public Path getOneTimeToken() {
    return oneTimeToken;
  }

  /**
   * Sets the path to the one-time token file.
   *
   * @param oneTimeToken path to the token file
   */
  public void setOneTimeToken(Path oneTimeToken) {
    this.oneTimeToken = oneTimeToken;
  }

  /**
   * Returns the JCA provider class to register.
   *
   * @return the provider class or {@code null}
   */
  public Class<? extends Provider> getProviderClass() {
    return providerClass;
  }

  /**
   * Sets the JCA provider class to register.
   *
   * @param providerClass provider implementation class
   */
  public void setProvider(Class<? extends Provider> providerClass) {
    this.providerClass = providerClass;
  }

  /**
   * Returns the configured storage provider type.
   *
   * @return the provider type
   */
  public KsmConfigProvider getProviderType() {
    return providerType;
  }

  /**
   * Sets the storage provider type.
   *
   * @param providerType the provider to use
   */
  public void setContainerType(KsmConfigProvider providerType) {
    this.providerType = providerType;
  }

  /**
   * Returns the user name used when accessing the secret container.
   *
   * @return the keystore or secret store user name
   */
  public String getSecretUser() {
    return secretUser;
  }

  /**
   * Sets the user name used when accessing the secret container.
   *
   * @param secretUser the user name
   */
  public void setSecretUser(String secretUser) {
    this.secretUser = secretUser;
  }

  /**
   * Returns the password used when accessing the secret container.
   *
   * @return the password
   */
  public String getSecretPassword() {
    return secretPassword;
  }

  /**
   * Sets the password used when accessing the secret container.
   *
   * @param secretPassword the password
   */
  public void setSecretPassword(String secretPassword) {
    this.secretPassword = secretPassword;
  }

  /**
   * Returns the path to the PKCS#11 library when using an HSM.
   *
   * @return the library path or {@code null}
   */
  public String getPkcs11Library() {
    return pkcs11Library;
  }

  /**
   * Sets the path to the PKCS#11 library for HSM access.
   *
   * @param pkcs11Library library path
   */
  public void setPkcs11Library(String pkcs11Library) {
    this.pkcs11Library = pkcs11Library;
  }

  /**
   * Whether the application should enforce IL‑5 readiness.
   *
   * @return {@code true} if IL‑5 enforcement is enabled
   */
  public boolean isEnforceIl5() {
    return enforceIl5;
  }

  /**
   * Enables or disables IL‑5 enforcement.
   *
   * @param enforceIl5 {@code true} to enforce IL‑5
   */
  public void setEnforceIl5(boolean enforceIl5) {
    this.enforceIl5 = enforceIl5;
  }

  /**
   * Returns the list of Keeper record specifiers to load as properties.
   *
   * @return list of record UIDs or folder/title combinations
   */
  public List<String> getRecords() {
    return records;
  }

  /**
   * Sets the Keeper records to load as configuration properties.
   *
   * @param records list of record specifiers
   */
  public void setRecords(List<String> records) {
    this.records = records;
  }

  @Override
  /**
   * Validates the configuration after properties are bound and applies defaults
   * where necessary.
   */
  public void afterPropertiesSet() throws Exception {
    if (enforceIl5 && !providerType.isIl5Ready()) {
      notIl5Compliant(providerType);
    }
    if (secretPath == null) {
      secretPath = Paths.get(providerType.getDefaultLocation());
    }
  }

  private void notIl5Compliant(KsmConfigProvider configProvider) {
    String message = "%s is not IL-5 compliant".formatted(configProvider);
    LOGGER.atError().log(message);
    throw new IllegalStateException(message);
  }

}
