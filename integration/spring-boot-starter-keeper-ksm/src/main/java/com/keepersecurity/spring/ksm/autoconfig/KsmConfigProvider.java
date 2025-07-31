package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Supported configuration providers for Keeper Secrets Manager.
 * <p>
 * Each enum constant specifies the default location of the configuration,
 * the Impact Level (IL) readiness, and the commercial compliance profile.
 */
public enum KsmConfigProvider {
    DEFAULT("kms-config.p12", SecurityLevel.IL2, SecurityProfile.NONE),
    NAMED("kms-config.p12", SecurityLevel.IL2, SecurityProfile.NONE),
    BC_FIPS("kms-config.bcfks", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    ORACLE_FIPS("kms-config.p12", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    /**
     * Uses the JVM's built-in SunPKCS11 provider.
     * <p>
     * The provider must be registered with a configuration file that
     * references your HSM's PKCS#11 library so the JDK can load the
     * native module. See the {@code SUN_PKCS11.md} document for details
     * on enabling the provider.
     * </p>
     */
    SUN_PKCS11("pkcs11://slot/0/token/kms", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    AWS("aws-secrets://region/resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    AZURE("azure-keyvault://vault/resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    AWS_HSM("aws-cloudhsm://resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    AZURE_HSM("azure-dedicatedhsm://resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    GOOGLE("gcp-secretmanager://project/resource", SecurityLevel.IL4, SecurityProfile.FEDRAMP_MODERATE),
    FORTANIX("fortanix://token", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    RAW("kms-config.json", SecurityLevel.IL2, SecurityProfile.NONE),
    HSM("pkcs11://slot/0/token/kms", SecurityLevel.IL5, SecurityProfile.FIPS_140_2);

    private final String defaultLocation;
    private final SecurityLevel ilLevel;
    private final SecurityProfile commercialProfile;

    KsmConfigProvider(String defaultLocation, SecurityLevel ilLevel, SecurityProfile commercialProfile) {
      if (isKeystoreBased()) {
        this.defaultLocation = KsmKeystoreDefaults.getDefaultKeystoreFilename();
      } else {
        this.defaultLocation = defaultLocation;
      }
        this.ilLevel = ilLevel;
        this.commercialProfile = commercialProfile;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }

    public SecurityLevel getIlLevel() {
        return ilLevel;
    }

    public SecurityProfile getCommercialProfile() {
        return commercialProfile;
    }

    public boolean isIl5Ready() {
        return ilLevel.compareTo(SecurityLevel.IL5) >= 0;
    }

    public boolean isCloudBased() {
        return this == AWS || this == AZURE || this == GOOGLE;
    }

    public boolean isKeystoreBased() {
        return this == DEFAULT || this == NAMED || this == BC_FIPS
            || this == ORACLE_FIPS;
    }

    public boolean isRaw() {
        return this == RAW;
    }

    public boolean isHsm() {
        return this == HSM || this == SUN_PKCS11
            || this == AWS_HSM || this == AZURE_HSM || this == FORTANIX;
    }

    public boolean isFedRAMPHigh() {
        return commercialProfile == SecurityProfile.FEDRAMP_HIGH;
    }
}

enum SecurityLevel {
    IL2, IL4, IL5, IL6
}

enum SecurityProfile {
    NONE,
    SOC2,
    ISO_27001,
    FEDRAMP_LOW,
    FEDRAMP_MODERATE,
    FEDRAMP_HIGH,
    FIPS_140_2
}
