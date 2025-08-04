package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Known hardware security module providers supported by the starter.
 */
public enum HsmProvider {
    /** Local development SoftHSM2 implementation. */
    SOFT_HSM2,
    /** JVM built-in SunPKCS11 provider. */
    SUN_PKCS11
}

