package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Known hardware security module providers supported by the starter.
 */
public enum HsmProvider {
  /** Local development SoftHSM2 implementation. */
  SOFT_HSM2(false),
  /** JVM built-in SunPKCS11 provider. */
  SUN_PKCS11(true),
  /** Bouncy Castle FIPS-approved provider. */
  BOUNCYCASTLE_FIPS(true),
  /** AWS CloudHSM FIPS-approved provider. */
  AWS_CLOUDHSM(true);

  private final boolean fipsApproved;

  HsmProvider(boolean fipsApproved) {
    this.fipsApproved = fipsApproved;
  }

  /**
   * Indicates whether this provider is FIPS 140-2 approved.
   *
   * @return {@code true} if FIPS approved
   */
  public boolean isFipsApproved() {
    return fipsApproved;
  }
}

