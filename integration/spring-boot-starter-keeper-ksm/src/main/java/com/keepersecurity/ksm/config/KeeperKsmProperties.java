package com.keepersecurity.ksm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Keeper Secrets Manager integration.
 */
@ConfigurationProperties(prefix = "keeper.ksm")
public class KeeperKsmProperties {

  private boolean enforceIl5 = false;
  private final Cache cache = new Cache();
  private final Config config = new Config();

  /**
   * Creates a new {@code KeeperKsmProperties} instance with default settings.
   */
  public KeeperKsmProperties() {
  }

  /**
   * Indicates whether IL‑5 compliance is enforced during initialization.
   *
   * @return {@code true} if IL‑5 enforcement is enabled
   */
  public boolean isEnforceIl5() {
    return enforceIl5;
  }

  /**
   * Enables or disables IL‑5 enforcement.
   *
   * @param enforceIl5 {@code true} to enforce IL‑5 compliance
   */
  public void setEnforceIl5(boolean enforceIl5) {
    this.enforceIl5 = enforceIl5;
  }

  /**
   * Returns cache-related configuration settings.
   *
   * @return cache configuration
   */
  public Cache getCache() {
    return cache;
  }

  /**
   * Returns Keeper configuration source settings.
   *
   * @return configuration source settings
   */
  public Config getConfig() {
    return config;
  }

  /**
   * Cache-related configuration options.
   */
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
     * Creates a new {@code Cache} instance with default settings.
     */
    public Cache() {
    }

    /**
     * Indicates whether caching is enabled.
     *
     * @return {@code true} if caching is enabled
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Enables or disables caching of secrets.
     *
     * @param enabled {@code true} to enable caching
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Indicates whether cached secrets are persisted across restarts.
     *
     * @return {@code true} if persistent caching is enabled
     */
    public boolean isPersist() {
      return persist;
    }

    /**
     * Enables or disables persistent cache storage.
     *
     * @param persist {@code true} to persist cached secrets
     */
    public void setPersist(boolean persist) {
      this.persist = persist;
    }

    /**
     * Indicates whether expired secrets may be served when the KSM
     * service cannot be reached.
     *
     * @return {@code true} to allow stale secrets if offline
     */
    public boolean isAllowStaleIfOffline() {
      return allowStaleIfOffline;
    }

    /**
     * Enables or disables returning stale secrets when the KSM service is
     * unavailable.
     *
     * @param allowStaleIfOffline {@code true} to allow stale secrets if offline
     */
    public void setAllowStaleIfOffline(boolean allowStaleIfOffline) {
      this.allowStaleIfOffline = allowStaleIfOffline;
    }
  }

  /**
   * Configuration source and HSM provider settings.
   */
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
     * Creates a new {@code Config} instance with default settings.
     */
    public Config() {
    }

    /**
     * Returns the configured HSM provider identifier.
     *
     * @return HSM provider name or {@code null}
     */
    public String getHsmProvider() {
      return hsmProvider;
    }

    /**
     * Sets the HSM provider identifier to use.
     *
     * @param hsmProvider provider name
     */
    public void setHsmProvider(String hsmProvider) {
      this.hsmProvider = hsmProvider;
    }

    /**
     * Returns the source location for Keeper configuration.
     *
     * @return configuration source
     */
    public String getSource() {
      return source;
    }

    /**
     * Sets the source location for Keeper configuration.
     *
     * @param source file path, URI or PKCS#11 locator
     */
    public void setSource(String source) {
      this.source = source;
    }

    /**
     * Indicates whether a one-time token fallback is permitted.
     *
     * @return {@code true} if fallback is allowed
     */
    public boolean isAllowFallback() {
      return allowFallback;
    }

    /**
     * Enables or disables fallback loading from a one-time token.
     *
     * @param allowFallback {@code true} to allow fallback
     */
    public void setAllowFallback(boolean allowFallback) {
      this.allowFallback = allowFallback;
    }
  }
}

