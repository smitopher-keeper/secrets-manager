# Local IL-5 Development

This guide explains how to configure a local project that mimics an IL-5 compliant environment by combining:

- **SunPKCS11** to bridge Java with a PKCS#11 token.
- **SoftHSM2** as a software HSM for development purposes.
- **Bouncy Castle FIPS** provider to supply FIPS 140 validated cryptography.

## 1. Install dependencies

- **SoftHSM2**
  - Ubuntu
    ```bash
    sudo apt-get install softhsm2
    ```
  - macOS
    ```bash
    brew install softhsm
    ```
- **Bouncy Castle FIPS**
  Add the library to your build. For Gradle:
  ```groovy
  implementation 'org.bouncycastle:bc-fips:2.1.0'
  ```
  Set the JVM in approved-only mode:
  ```bash
  export JAVA_TOOL_OPTIONS="-Dorg.bouncycastle.fips.approved_only=true"
  ```

## 2. Initialize a SoftHSM2 token

Create a token with a known PIN:
```bash
softhsm2-util --init-token --slot 0 --label ksm-il5
```

## 3. Register the Bouncy Castle FIPS provider

Ensure the provider is loaded before your application starts:
```java
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import java.security.Security;

Security.insertProviderAt(new BouncyCastleFipsProvider(), 1);
```

## 4. Configure SunPKCS11

1. Enable the `jdk.crypto.cryptoki` module so the SunPKCS11 provider is available:
   ```bash
   export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS} --add-modules=jdk.crypto.cryptoki"
   ```
2. Create a config file such as `pkcs11.cfg` pointing to the SoftHSM2 library:
   ```
   name = SoftHsm
   library = /usr/lib/softhsm/libsofthsm2.so   # or /usr/local/lib/softhsm/libsofthsm2.so
   slot = 0
   ```
3. Register the provider. Either edit `java.security`:
   ```
   security.provider.9=SunPKCS11 /path/to/pkcs11.cfg
   ```
   or load it programmatically:
   ```java
   Security.addProvider(new SunPKCS11("/path/to/pkcs11.cfg"));
   ```

## 5. Configure the Spring Boot starter

In `application.yml` point the starter to the PKCS#11 token and enable IL‑5 enforcement:
```yaml
keeper:
  ksm:
    provider: org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
    pkcs11:
      library: /usr/lib/softhsm/libsofthsm2.so # adjust for your OS
    enforce-il5: true
```

Start your application. The starter will use the SoftHSM2 token through SunPKCS11 and all cryptographic operations will be handled by the Bouncy Castle FIPS provider, providing a local setup that emulates an IL‑5 compliant environment.
