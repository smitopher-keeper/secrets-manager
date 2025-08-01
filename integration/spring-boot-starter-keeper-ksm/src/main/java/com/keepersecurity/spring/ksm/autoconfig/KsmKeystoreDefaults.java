package com.keepersecurity.spring.ksm.autoconfig;

import java.security.KeyStore;
import java.security.Provider;

/**
 * Utility methods for resolving keystore file defaults.
 */
public final class KsmKeystoreDefaults {

  private KsmKeystoreDefaults() {/* prevent instantiation */}

  /**
   * Determines the default keystore extension for the current JVM provider.
   *
   * @return the default file extension such as {@code p12} or {@code bcfks}
   */
  public static String resolveDefaultExtension() {
      try {
          KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType()); // usually PKCS12
          Provider provider = ks.getProvider();
          String providerName = provider.getName().toLowerCase();

          if (providerName.contains("bcfips")) {
            return "bcfks";
          }
          if (providerName.contains("bc")) {
            return "bks";
          }
          if (providerName.contains("sun") && ks.getType().equalsIgnoreCase("PKCS12")) {
            return "p12";
          }
          if (ks.getType().equalsIgnoreCase("JKS")) {
            return "jks";
          }

      } catch (Exception e) {
          // fallback
      }
      return "p12";
  }

  /**
   * Provides the default keystore filename using the extension returned by
   * {@link #resolveDefaultExtension()}.
   *
   * @return the default keystore file name
   */
  public static String getDefaultKeystoreFilename() {
      return "kms-config." + resolveDefaultExtension();
  }
}
