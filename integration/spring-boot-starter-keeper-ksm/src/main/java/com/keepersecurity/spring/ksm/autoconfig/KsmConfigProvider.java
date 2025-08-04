package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Supported configuration providers for Keeper Secrets Manager.
 *
 * <p>Each constant represents a mechanism for storing the KSM configuration
 * along with the default location for that configuration, the Impact Level
 * readiness and the commercial compliance profile.
 */
public enum KsmConfigProvider {
    /** Default keystore using the platform provider. */
    DEFAULT("ksm-config.p12", SecurityLevel.IL2, SecurityProfile.NONE),
    /** A named keystore entry using the platform provider. */
    NAMED("ksm-config.p12", SecurityLevel.IL2, SecurityProfile.NONE),
    /** Keystore using the BouncyCastle FIPS provider. */
    BC_FIPS("ksm-config.bcfks", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    /** Keystore using the Oracle FIPS provider. */
    ORACLE_FIPS("ksm-config.p12", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    /**
     * Uses the JVM's built-in SunPKCS11 provider.
     * <p>
     * The provider must be registered with a configuration file that
     * references your HSM's PKCS#11 library so the JDK can load the
     * native module. See the {@code SUN_PKCS11.md} document for details
     * on enabling the provider.
     * </p>
     */
    /** Configuration stored in an HSM using the SunPKCS11 provider. */
    SUN_PKCS11("pkcs11://slot/0/token/kms", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    /** Configuration stored in AWS Secrets Manager. */
    AWS("aws-secrets://region/resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    /** Configuration stored in Azure Key Vault. */
    AZURE("azure-keyvault://vault/resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    /** Configuration stored in AWS CloudHSM. */
    AWS_HSM("aws-cloudhsm://resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    /** Configuration stored in Azure Dedicated HSM. */
    AZURE_HSM("azure-dedicatedhsm://resource", SecurityLevel.IL5, SecurityProfile.FEDRAMP_HIGH),
    /** Configuration stored in Google Secret Manager. */
    GOOGLE("gcp-secretmanager://project/resource", SecurityLevel.IL4, SecurityProfile.FEDRAMP_MODERATE),
    /** Configuration stored in a Fortanix DSM. */
    FORTANIX("fortanix://token", SecurityLevel.IL5, SecurityProfile.FIPS_140_2),
    /** Raw JSON configuration on the filesystem. */
    RAW("ksm-config.json", SecurityLevel.IL2, SecurityProfile.NONE),
    /** Generic PKCS#11 HSM configuration. */
    HSM("pkcs11://slot/0/token/kms", SecurityLevel.IL5, SecurityProfile.FIPS_140_2);

    private final String defaultLocation;
    private final SecurityLevel ilLevel;
    private final SecurityProfile commercialProfile;

    /**
     * Creates a new provider definition.
     *
     * @param defaultLocation the default location for this provider
     * @param ilLevel the Impact Level associated with the provider
     * @param commercialProfile the commercial compliance profile
     */
    KsmConfigProvider(String defaultLocation, SecurityLevel ilLevel, SecurityProfile commercialProfile) {
      if (isKeystoreBased()) {
        this.defaultLocation = KsmKeystoreDefaults.getDefaultKeystoreFilename();
      } else {
        this.defaultLocation = defaultLocation;
      }
        this.ilLevel = ilLevel;
        this.commercialProfile = commercialProfile;
    }

    /**
     * Returns the default configuration location for the provider.
     *
     * @return the default configuration path or URI
     */
    public String getDefaultLocation() {
        return defaultLocation;
    }

    /**
     * Returns the Impact Level the provider is ready for.
     *
     * @return the security level
     */
    public SecurityLevel getIlLevel() {
        return ilLevel;
    }

    /**
     * Returns the commercial compliance profile for this provider.
     *
     * @return the compliance profile
     */
    public SecurityProfile getCommercialProfile() {
        return commercialProfile;
    }

    /**
     * Indicates whether this provider meets or exceeds IL-5 readiness.
     *
     * @return {@code true} if IL5 or higher
     */
    public boolean isIl5Ready() {
        return ilLevel.compareTo(SecurityLevel.IL5) >= 0;
    }

    /**
     * Determines if the configuration is stored in a cloud service.
     *
     * @return {@code true} for AWS, Azure or Google providers
     */
    public boolean isCloudBased() {
        return this == AWS || this == AZURE || this == GOOGLE;
    }

    /**
     * Determines if the configuration is stored in a Java keystore.
     *
     * @return {@code true} if keystore based
     */
    public boolean isKeystoreBased() {
        return this == DEFAULT || this == NAMED || this == BC_FIPS
            || this == ORACLE_FIPS;
    }

    /**
     * Determines if the provider stores configuration as raw JSON.
     *
     * @return {@code true} for the RAW provider
     */
    public boolean isRaw() {
        return this == RAW;
    }

    /**
     * Determines if the configuration is stored in an HSM.
     *
     * @return {@code true} if the provider uses an HSM backend
     */
    public boolean isHsm() {
        return this == HSM || this == SUN_PKCS11
            || this == AWS_HSM || this == AZURE_HSM || this == FORTANIX;
    }

    /**
     * Indicates whether the provider meets the FedRAMP High profile.
     *
     * @return {@code true} if FedRAMP High compliant
     */
    public boolean isFedRAMPHigh() {
        return commercialProfile == SecurityProfile.FEDRAMP_HIGH;
    }
}

/**
 * Impact Level readiness of a provider.
 */
enum SecurityLevel {
    /** Suitable for IL2 workloads. */
    IL2,
    /** Suitable for IL4 workloads. */
    IL4,
    /** Suitable for IL5 workloads. */
    IL5,
    /** Suitable for IL6 workloads. */
    IL6
}

/**
 * Commercial compliance profile of a provider.
 */
enum SecurityProfile {
    /** No specific compliance guarantees. */
    NONE,
    /** SOC 2 compliant. */
    SOC2,
    /** ISO 27001 compliant. */
    ISO_27001,
    /** FedRAMP Low compliant. */
    FEDRAMP_LOW,
    /** FedRAMP Moderate compliant. */
    FEDRAMP_MODERATE,
    /** FedRAMP High compliant. */
    FEDRAMP_HIGH,
    /** FIPS 140-2 compliant. */
    FIPS_140_2
}
