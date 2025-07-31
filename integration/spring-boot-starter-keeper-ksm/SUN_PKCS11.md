# Enabling the Sun PKCS#11 Provider

The `SUN_PKCS11` option uses the JDK's built‑in SunPKCS11 security provider. This
provider is disabled by default and must be loaded with a configuration file that
points to your HSM's PKCS#11 library.

1. Create a small config file, for example `pkcs11.cfg`:
   ```
   name = MyHsm
   library = /path/to/libpkcs11.so
   slot = 0
   ```
2. Register the provider before the application starts. You can edit
   `${JAVA_HOME}/conf/security/java.security` to add:
   ```
   security.provider.9=SunPKCS11 /path/to/pkcs11.cfg
   ```
   or load it programmatically:
   ```java
   Security.addProvider(new SunPKCS11("/path/to/pkcs11.cfg"));
   ```

## Why Enable It?

Keeper uses the provider to store the configuration on the HSM when the
`SUN_PKCS11` profile is selected. Without registering the provider the starter
cannot access the PKCS#11 token and initialization will fail.
