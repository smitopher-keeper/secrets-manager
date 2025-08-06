package com.keepersecurity.ksm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration properties for Keeper Secrets Manager integration.
 */
@ConfigurationProperties(prefix = "keeper.ksm")
@Slf4j
@Getter
@Setter
public class KeeperKsmProperties {

  /**
   * Indicates whether IL‑5 compliance is enforced during initialization.
   */
  private boolean enforceIl5 = false;

  /**
   * Cache-related configuration settings.
   */
  private final Cache cache = new Cache();

  /**
   * Keeper configuration source settings.
   */
  private final Config config = new Config();

  /**
   * Creates a new {@code KeeperKsmProperties} instance with default settings.
   */
  public KeeperKsmProperties() {
  }

  /**
   * Cache-related configuration options.
   */
  @Getter
  @Setter
  public static class Cache {
    /**
     * Enables in-memory caching of Keeper secrets.
     */
    private boolean enabled = true;

    /**
     * Enables persistent cache storage (e.g., on disk).
     */
    private boolean persist = true;

    /**
     * If true, expired secrets may still be returned from cache
     * when the KSM service is unreachable.
     * Default is false — expired secrets will cause a failure.
     */
    private boolean allowStaleIfOffline = false;

    /**
     * Private constructor to prevent external instantiation.
     */
    private Cache() {
    }

  }

  /**
   * Configuration source and HSM provider settings.
   */
  @Getter
  @Setter
  public static class Config {
    /**
     * HSM provider type (e.g., "pkcs11", "softhsm2", "bouncycastle-fips").
     */
    private String hsmProvider;

    /**
     * Path to load Keeper configuration (file, URI, or PKCS#11).
     */
    private String source;

    /**
     * Whether to allow fallback loading from a one-time-token.
     */
    private boolean allowFallback = true;

    /**
     * Private constructor to prevent external instantiation.
     */
    private Config() {
    }

  }
}

