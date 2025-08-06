package com.keepersecurity.spring.ksm.autoconfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.keepersecurity.secretsManager.core.LocalConfigStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Holder for properties in the {@code keeper.ksm.*} namespace.
 * <p>
 * The values are bound from application configuration files so that the
 * {@link KeeperKsmAutoConfiguration} can configure the SDK appropriately.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "keeper.ksm")
@Slf4j
public class KeeperKsmProperties implements InitializingBean {

  /**
   * Creates a new {@code KeeperKsmProperties} instance. The constructor is package-private so that
   * Spring can perform configuration binding while keeping the type out of the public API.
   */
  KeeperKsmProperties() {}

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
   * The starter reads the token from this file, redeems it to generate the configuration and then
   * deletes the file. After a successful run the application terminates and you should remove this
   * property from your configuration.
   */
  private Path oneTimeToken;

  /**
   * Fully qualified class name of a JCA security provider to register. Optional. If not set, a
   * provider will be chosen automatically.
   */
  private Class<? extends Provider> providerClass;

  /**
   * Type of secret container to use. Supported values correspond to the constants in
   * {@link KsmConfigProvider} such as {@code default}, {@code named}, {@code bc_fips},
   * {@code oracle_fips}, {@code sun_pkcs11}, {@code softhsm2}, {@code aws}, {@code azure},
   * {@code aws_hsm}, {@code azure_hsm}, {@code google}, {@code fortanix}, {@code raw}, {@code hsm}.
   * For raw JSON configuration, use {@link KsmConfigProvider#RAW}. Defaults to
   * {@link KsmConfigProvider#DEFAULT}.
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
   * When set to {@link HsmProvider#SOFT_HSM2}, the starter will attempt to auto-detect the SoftHSM2
   * library path. {@link HsmProvider#SUN_PKCS11} indicates that the JDK's built in SunPKCS11
   * provider is used.
   * </p>
   */
  private HsmProvider hsmProvider;

  /**
   * When true, the application will fail to start unless the IL-5 certified provider is available.
   * This can be used to enforce IL-5 compliance.
   */
  private boolean enforceIl5 = false;

  /**
   * List of Keeper record identifiers to load and expose as configuration properties. Each entry
   * may be a record UID or a string in the form {@code "folder/record"} which will be resolved to
   * the record UID at runtime.
   */
  private List<String> records = new ArrayList<>();

  /**
   * Configuration for Keeper SDK secret caching.
   */
  private CacheProperties cache = new CacheProperties();

  @Override
  /**
   * Validates the configuration after properties are bound and applies defaults where necessary.
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
      String message =
          "Configured HSM provider is not FIPS-approved. IL5 enforcement requires a FIPS-compliant PKCS#11 provider.";
      log.atError().log(message);
      throw new IllegalStateException(message);
    }
  }

  private void notIl5Compliant(KsmConfigProvider configProvider) {
    String message = "%s is not IL-5 compliant".formatted(configProvider);
    log.atError().log(message);
    throw new IllegalStateException(message);
  }

  /**
   * Caching-related properties under {@code keeper.ksm.cache}. Custom persistent storage can be
   * supplied by defining a {@code ConfigStorage} Spring bean.
   */
  @Getter
  @Setter
  public static class CacheProperties {

    /**
     * Whether Keeper SDK secret caching is enabled. Defaults to {@code true}.
     */
    private boolean enabled = true;

    /**
     * Whether cached secrets are persisted to disk using {@link LocalConfigStorage}, which stores
     * encrypted cache data on disk. Defaults to {@code true}.
     */
    private boolean persist = true;

    /**
     * File path to persist the cached secrets (default: {@code ~/.keeper/ksm/ksm-cache.json}).
     * Ignored if {@link #persist} is {@code false}. The file is encrypted internally by the SDK.
     */
    private String path = System.getProperty("user.home") + "/.keeper/ksm/ksm-cache.json";

    /**
     * Time-to-live for cached secrets, in seconds. After this duration secrets are re-fetched from
     * Keeper. Applies to both in-memory and persistent cache backends. Default: 300 seconds (5
     * minutes).
     */
    private Duration ttl = Duration.ofSeconds(300);

    /**
     * If true, expired secrets may still be returned from cache when the KSM service is
     * unreachable. Default is {@code false} for stricter environments such as IL5.
     */
    private boolean allowStaleIfOffline = false;

    /**
     * Private constructor to prevent external instantiation.
     */
    private CacheProperties() {}

  }

}
