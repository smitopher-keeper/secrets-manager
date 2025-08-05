package com.keepersecurity.spring.ksm.autoconfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.keepersecurity.secretsManager.core.LocalConfigStorage;

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
   * Known HSM provider to assist with PKCS#11 configuration. Optional.
   * <p>
   * When set to {@link HsmProvider#SOFT_HSM2}, the starter will attempt to
   * auto-detect the SoftHSM2 library path. {@link HsmProvider#SUN_PKCS11}
   * indicates that the JDK's built in SunPKCS11 provider is used.
   * </p>
   */
  private HsmProvider hsmProvider;

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

  /**
   * Configuration for Keeper SDK secret caching.
   */
  private CacheProperties cache = new CacheProperties();

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
   * Returns the configured HSM provider, if any.
   *
   * @return the selected {@link HsmProvider} or {@code null}
   */
  public HsmProvider getHsmProvider() {
    return hsmProvider;
  }

  /**
   * Sets the HSM provider to use for PKCS#11 operations.
   *
   * @param hsmProvider provider identifier
   */
  public void setHsmProvider(HsmProvider hsmProvider) {
    this.hsmProvider = hsmProvider;
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

  /**
   * Returns the caching configuration.
   *
   * @return cache configuration properties
   */
  public CacheProperties getCache() {
    return cache;
  }

  /**
   * Sets the caching configuration.
   *
   * @param cache caching properties
   */
  public void setCache(CacheProperties cache) {
    this.cache = cache;
  }

  @Override
  /**
   * Validates the configuration after properties are bound and applies defaults
   * where necessary.
   */
  public void afterPropertiesSet() throws Exception {
    if (enforceIl5) {
      validateHsmProvider();
      if (!providerType.isIl5Ready()) {
        notIl5Compliant(providerType);
      }
    }
    if (secretPath == null) {
      secretPath = Paths.get(providerType.getDefaultLocation());
    }
  }

  private void validateHsmProvider() {
    if (hsmProvider == null || !hsmProvider.isFipsApproved()) {
      String message = "Configured HSM provider is not FIPS-approved. IL5 enforcement requires a FIPS-compliant PKCS#11 provider.";
      LOGGER.atError().log(message);
      throw new IllegalStateException(message);
    }
  }

  private void notIl5Compliant(KsmConfigProvider configProvider) {
    String message = "%s is not IL-5 compliant".formatted(configProvider);
    LOGGER.atError().log(message);
    throw new IllegalStateException(message);
  }

  /**
   * Caching-related properties under {@code keeper.ksm.cache}.
   * Custom persistent storage can be supplied by defining a
   * {@code ConfigStorage} Spring bean.
   */
  public static class CacheProperties {

    /**
     * Whether Keeper SDK secret caching is enabled. Defaults to {@code true}.
     */
    private boolean enabled = true;

    /**
     * Whether cached secrets are persisted to disk using
     * {@link LocalConfigStorage}, which stores encrypted cache data on disk.
     * Defaults to {@code true}.
     */
    private boolean persist = true;

    /**
     * File path to persist the cached secrets (default:
     * {@code ~/.keeper/ksm/ksm-cache.json}). Ignored if {@link #persist} is
     * {@code false}. The file is encrypted internally by the SDK.
     */
    private String path = System.getProperty("user.home") + "/.keeper/ksm/ksm-cache.json";

    /**
     * Time-to-live for cached secrets, in seconds. After this duration secrets
     * are re-fetched from Keeper. Applies to both in-memory and persistent
     * cache backends. Default: 300 seconds (5 minutes).
     */
    private Duration ttl = Duration.ofSeconds(300);

    /**
     * Returns whether caching is enabled.
     *
     * @return {@code true} to enable caching
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Enables or disables caching.
     *
     * @param enabled {@code true} to enable caching
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Returns whether cached secrets are persisted to disk.
     *
     * @return {@code true} to persist cached secrets
     */
    public boolean isPersist() {
      return persist;
    }

    /**
     * Enables or disables persistent caching.
     *
     * @param persist {@code true} to persist cached secrets
     */
    public void setPersist(boolean persist) {
      this.persist = persist;
    }

    /**
     * Returns the file path used to persist cached secrets.
     *
     * @return path to the persistent cache file
     */
    public String getPath() {
      return path;
    }

    /**
     * Sets the file path used to persist cached secrets. If blank or {@code null}
     * the default path is used.
     *
     * @param path location of the persistent cache file
     */
    public void setPath(String path) {
      this.path = path;
    }

    /**
     * Returns the time-to-live for cached secrets.
     *
     * @return cache TTL duration
     */
    public Duration getTtl() {
      return ttl;
    }

    /**
     * Sets the time-to-live for cached secrets.
     *
     * @param ttl cache TTL duration
     */
    public void setTtl(Duration ttl) {
      this.ttl = ttl;
    }
  }

}
