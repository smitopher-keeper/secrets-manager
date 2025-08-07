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

Enable IL‑5 enforcement in your application configuration:
```yaml
keeper:
  ksm:
    enforce-il5: true
```

Provide PKCS#11 access through a custom Spring configuration:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import sun.security.pkcs11.SunPKCS11;
import java.security.Security;

@Configuration
public class Il5HsmConfiguration {
  @Bean
  SecretsManagerOptions hsmOptions() {
    Security.addProvider(new SunPKCS11("/path/to/pkcs11.cfg"));
    KeyValueStorage storage = new Pkcs11ConfigStorage(
        "pkcs11://slot/0/token/ksm-il5", "<PIN>".toCharArray());
    return new SecretsManagerOptions(storage);
  }
}
```

Start your application. The starter uses the SoftHSM2 token through SunPKCS11 and all cryptographic operations are handled by the Bouncy Castle FIPS provider, providing a local setup that emulates an IL‑5 compliant environment.
