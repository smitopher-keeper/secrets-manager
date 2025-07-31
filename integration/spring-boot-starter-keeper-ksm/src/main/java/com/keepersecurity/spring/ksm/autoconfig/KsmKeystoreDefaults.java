package com.keepersecurity.spring.ksm.autoconfig;

import java.security.KeyStore;
import java.security.Provider;

public final class KsmKeystoreDefaults {

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

  public static String getDefaultKeystoreFilename() {
      return "kms-config." + resolveDefaultExtension();
  }
}
