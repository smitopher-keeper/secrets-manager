# Enabling the Sun PKCS#11 Provider

The `SUN_PKCS11` option uses the JDK's built‑in SunPKCS11 security provider. This
provider is disabled by default and must be loaded with a configuration file that
points to the PKCS#11 module supplied by your HSM vendor.

1. Enable the provider's module so it can be discovered at runtime. Add the
   `jdk.crypto.cryptoki` module to the JVM:
   ```
   java --add-modules jdk.crypto.cryptoki ...
   ```
   You can also set `JAVA_TOOL_OPTIONS=--add-modules=jdk.crypto.cryptoki` to apply
   it automatically.
2. Create a small config file, for example `pkcs11.cfg`:
   ```
   name = MyHsm
   library = /path/to/libpkcs11.so
   slot = 0
   ```
3. Register the provider before the application starts. You can edit
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

After registering the provider, create a custom `@Configuration` class that
exposes a `SecretsManagerOptions` bean using a PKCS#11-backed storage to connect
the Spring Boot starter with your HSM.
