package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Simple representation of a PKCS#11 configuration describing the path to the native library.
 */
public record PKCS11Config(String libraryPath) {}

