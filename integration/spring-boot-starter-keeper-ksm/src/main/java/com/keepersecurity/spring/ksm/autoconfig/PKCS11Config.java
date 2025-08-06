package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Simple representation of a PKCS#11 configuration.
 *
 * @param libraryPath path to the native PKCS#11 library
 */
public record PKCS11Config(String libraryPath) {}
