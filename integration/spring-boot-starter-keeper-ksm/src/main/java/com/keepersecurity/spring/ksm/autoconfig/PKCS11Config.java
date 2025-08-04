package com.keepersecurity.spring.ksm.autoconfig;

/**
 * Simple representation of a PKCS#11 configuration describing the path to the native library.
 */
public class PKCS11Config {

    private final String libraryPath;

    /**
     * Creates a new configuration instance.
     *
     * @param libraryPath path to the native PKCS#11 library
     */
    public PKCS11Config(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    /**
     * Returns the path to the PKCS#11 library.
     *
     * @return library path as a string
     */
    public String getLibraryPath() {
        return libraryPath;
    }
}
