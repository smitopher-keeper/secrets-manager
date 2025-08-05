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

  public boolean isEnforceIl5() {
    return enforceIl5;
  }

  public void setEnforceIl5(boolean enforceIl5) {
    this.enforceIl5 = enforceIl5;
  }

  public Cache getCache() {
    return cache;
  }

  public Config getConfig() {
    return config;
  }

  public static class Cache {
    /**
     * Enables in-memory caching of Keeper secrets.
     */
    private boolean enabled = true;

    /**
     * Enables persistent cache storage (e.g., on disk).
     */
    private boolean persist = true;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isPersist() {
      return persist;
    }

    public void setPersist(boolean persist) {
      this.persist = persist;
    }
  }

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

    public String getHsmProvider() {
      return hsmProvider;
    }

    public void setHsmProvider(String hsmProvider) {
      this.hsmProvider = hsmProvider;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    public boolean isAllowFallback() {
      return allowFallback;
    }

    public void setAllowFallback(boolean allowFallback) {
      this.allowFallback = allowFallback;
    }
  }
}

