# Keeper Secrets Manager Spring Boot Starter

This starter enables easy integration of [Keeper Secrets Manager](https://keepersecurity.com/secrets-manager.html) into Spring Boot applications. It provides auto-configuration to load Keeper Secrets Manager (KSM) credentials and to create a `SecretsManagerOptions` bean for retrieving secrets.

## Usage

### Dependency

Add the starter dependency to your project. For example, with Gradle:

```groovy
implementation 'com.keepersecurity.secrets-manager:keeper-secrets-manager-spring-boot-starter:1.0.0-SNAPSHOT'
```

This will transitively include the Keeper Secrets Manager SDK and required Spring Boot auto-configuration support.

### Configuration

You can configure the KSM credentials via Spring Boot properties (e.g., in your `application.properties` or YAML):

- **Option 1: One-Time Token (OTT)** – Use this for first-time setup. Provide the **path** to a file that contains the one-time access token given by Keeper:
```properties
keeper.ksm.one-time-token = path/to/one-time-token.txt
keeper.ksm.secret-path = path/to/ksm-config.json
```
On the first run, the starter reads the token from the file, redeems it to retrieve your KSM configuration and save it to the specified JSON file. The token file is deleted **after** the configuration is generated and the starter throws a `OneTimeTokenConsumedException` to stop the application. **Note:** one-time tokens can only be used once; restart the application after removing the `keeper.ksm.one-time-token` property from your configuration.

When `keeper.ksm.enforce-il5=true`, this one-time-token bootstrapping is blocked to maintain IL5 compliance. You may override this behavior by setting `bootstrap.check.mode=warn` to merely log a warning.

IL5 enforcement also requires audit logging. Configure a logger named `com.keepersecurity` or `com.keepersecurity.ksm` at level `INFO` or higher and direct it to a secure sink such as a file appender. If no audit sink is detected the application fails to start by default. Override this behavior with `audit.check.mode=warn` to only log a warning.

- **Option 2: Existing Config File** – If you already have a Keeper config JSON (e.g., from a previous initialization), you can just specify:
  ```properties
  keeper.ksm.secret-path = path/to/ksm-config.json
  ```
  Ensure the file is accessible at runtime. The starter will load this file to configure access to Keeper Secrets Manager.

- **Secret Container Options** – The config can be stored in different container types. By default a local file is used. Set the container type and optional credentials if needed:
  ```properties
  keeper.ksm.container-type = pkcs11
  keeper.ksm.hsm-provider = softHsm2
  keeper.ksm.pkcs11-library = /path/to/lib.so
  keeper.ksm.secret-user = changeme
  keeper.ksm.secret-password = changeme
  ```
When `container-type` is `pkcs11`, this starter uses `Pkcs11ConfigStorage`. This class only stores the Keeper configuration in memory and **does not** load the specified library as a real PKCS#11 provider. A full PKCS#11 integration would require additional implementation.

### Supported Container Types

The `keeper.ksm.container-type` property accepts the following values.

| Value | Default location | Security level | Compliance profile | Notes |
|-------|-----------------|----------------|--------------------|-------|
| `default` | `ksm-config.p12` | IL-2 | None | default Java keystore |
| `named`   | `ksm-config.p12` | IL-2 | None | named keystore entry |
| `bc_fips` | `ksm-config.bcfks` | IL-5 | FIPS 140-2 | requires Bouncy Castle FIPS |
| `oracle_fips` | `ksm-config.p12` | IL-5 | FIPS 140-2 | Oracle FIPS provider |
| `sun_pkcs11` | `pkcs11://slot/0/token/kms` | IL-5 | FIPS 140-2 | Sun PKCS#11 provider |
| `aws`     | `aws-secrets://region/resource` | IL-5 | FedRAMP High | requires AWS SDK |
| `azure`   | `azure-keyvault://vault/resource` | IL-5 | FedRAMP High | requires Azure SDK |
| `aws_hsm` | `aws-cloudhsm://resource` | IL-5 | FedRAMP High | PKCS#11 via CloudHSM |
| `azure_hsm` | `azure-dedicatedhsm://resource` | IL-5 | FedRAMP High | PKCS#11 via Azure HSM |
| `google`  | `gcp-secretmanager://project/resource` | IL-4 | FedRAMP Moderate | requires Google SDK |

| `raw`     | `ksm-config.json` | IL-2 | None | plain JSON file |
| `hsm`     | `pkcs11://slot/0/token/kms` | IL-5 | FIPS 140-2 | generic PKCS#11 HSM |
| `fortanix` | `fortanix://token` | IL-5 | FIPS 140-2 | Fortanix DSM via PKCS#11 |

**Caution:** The `raw` container stores secrets in clear text and should only be used for testing or other non-production environments.

AWS CloudHSM, Azure Dedicated HSM, Fortanix DSM and the generic `hsm` provider
persist the configuration using a PKCS#11-backed keystore. Ensure the
appropriate vendor PKCS#11 library and security provider are available on the
classpath when using these options.

### IL-5 Provider Summary

The following providers from the
[`KsmConfigProvider`](src/main/java/com/keepersecurity/spring/ksm/autoconfig/KsmConfigProvider.java)
enum are IL-5 ready. Each entry complies with the corresponding security
profile indicated below:

| Provider | Enum constant | Compliance highlights |
|----------|---------------|-----------------------|
| Bouncy Castle FIPS Keystore | `BC_FIPS` | Utilizes the Bouncy Castle FIPS provider for FIPS 140-2 compliance. |
| Oracle FIPS Keystore | `ORACLE_FIPS` | Oracle JCE FIPS provider for FIPS 140-2 compliance. |
| Sun PKCS#11 Provider | `SUN_PKCS11` | Uses the built-in Sun PKCS#11 provider in FIPS mode. |
| AWS Secrets Manager | `AWS` | Cloud-based integration that meets FedRAMP High. |
| Azure Key Vault | `AZURE` | Cloud-based integration that meets FedRAMP High. |
| AWS CloudHSM | `AWS_HSM` | Cloud-based HSM that meets FedRAMP High. |
| Azure Dedicated HSM | `AZURE_HSM` | Cloud-based HSM that meets FedRAMP High. |
| Hardware Security Module | `HSM` | Stores configuration through PKCS#11, providing FIPS 140-2 compliance. |
| Fortanix DSM | `FORTANIX` | Integrates with Fortanix DSM for FIPS 140-2 compliance. |

All of the above return `true` from `isIl5Ready()` in `KsmConfigProvider`,
signaling that they meet IL-5 security requirements.

- **Key Store Placeholder** – You can store the configuration in a Java KeyStore instead of a plain file. Set the container type to `keystore` and optionally provide your own alias and password:
  ```properties
  keeper.ksm.container-type = keystore
  keeper.ksm.secret-user = ksm-config
  keeper.ksm.secret-password = changeme
  ```
  If the Bouncy Castle provider is available on the classpath, the starter will use a `BCKS` store; otherwise it falls back to the standard `JKS`. A clear text JSON file is still used under the hood. The default path is `~/.keeper/ksm/ksm-config.<shape>` where `<shape>` resolves to `bcks`, `jks`, or `json` depending on the chosen storage type.

- **Optional Provider Selection** – You can specify the security provider class explicitly:
  ```properties
  keeper.ksm.provider = org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
  ```
  If not set, the starter will attempt to load the Bouncy Castle FIPS provider, fall back to the regular Bouncy Castle provider, and finally use the JVM default provider.

- **IL-5 Enforcement** – To require the Bouncy Castle FIPS provider and fail startup if it is not present:
  ```properties
  keeper.ksm.enforce-il5 = true
  ```

- **Record Selection** – Specify which Keeper records to load. Each entry can be
  a record UID or a `folder/record` path:
  ```yaml
  keeper:
    ksm:
      records:
        - certs/my-keypair
        - db/my-datasource
        - api/my-thirdparty
  ```
  Once the starter is on the classpath, the fields from these records become
  available using keys like `Ue8h6JyWUs7Iu6eY_mha-w.password`.

### 💾 Persistent Secret Caching

This starter enables Keeper’s persistent cache by default to reduce repeated secret lookups.

Configure the cache location or disable persistence:

```yaml
keeper:
  ksm:
    cache:
      persist: true
      path: /etc/ksm/secure-cache.json  # Optional; defaults to ~/.keeper/ksm/ksm-cache.json
      ttl: 10m                          # Optional; defaults to 300s
```

To disable all caching:

```yaml
keeper:
  ksm:
    cache:
      enabled: false
```

To keep in-memory caching but skip persistence:

```yaml
keeper:
  ksm:
    cache:
      enabled: true
      persist: false
```

To tolerate temporary KSM outages and still read secrets from the cache, enable
the fallback to expired entries:

```yaml
keeper:
  ksm:
    cache:
      allow-stale-if-offline: true   # Allows expired secrets if KSM is down
```

Use with caution. Enabling this may help during outages, but stale secrets may
violate IL5 security posture.

You may also override the caching strategy by defining a custom `ConfigStorage` bean:

```java
@Bean
public ConfigStorage configStorage() {
    return new EncryptedFileConfigStorage("/secure/ksm-cache.enc", masterKey);
}
```

### Sun PKCS#11 Requirements

Using the `SUN_PKCS11` provider requires the JDK's SunPKCS11 security provider
to be configured with a file that references your HSM's PKCS#11 library. Ensure
the provider is registered before the application starts and specify the library
path with `keeper.ksm.pkcs11-library` when using the `pkcs11` container type.
For detailed steps see [SUN_PKCS11.md](SUN_PKCS11.md).

If neither property is set, the auto-configuration will not initialize and will throw an error to remind you to configure KSM credentials.

### Using SoftHSM2 for IL5 Emulation

SoftHSM2 can emulate an IL5 HSM for local development and testing.

- **Installation**

  ```bash
  brew install softhsm     # macOS
  sudo apt install softhsm2 # Debian/Ubuntu
  ```

- **Configuration**

  ```yaml
  ksm:
    config:
      hsm-provider: softhsm2
      enforce-il5: false
  ```

  Setting `enforce-il5: true` forbids SoftHSM2 and causes the starter to fail fast.

- **Troubleshooting**

  Errors about missing PKCS#11 libraries usually mean the `libsofthsm2.so` module is not installed or not on the search path. Install the library and ensure the `SOFTHSM2_MODULE` environment variable points to the correct location.

For more details see the [Keeper HSM documentation](https://docs.keeper.io/secrets-manager/secrets-manager/hsm-integration).

### Optional Cloud and HSM Providers

To keep the starter lightweight, SDKs for providers like AWS Secrets Manager, Azure Key Vault, and Google Secret Manager are not included. Add the corresponding dependency to your application's build file to enable a provider. For example:

```groovy
implementation 'software.amazon.awssdk:secretsmanager:2.20.28'
implementation 'com.azure:azure-security-keyvault-secrets:4.9.2'
implementation 'com.google.cloud:google-cloud-secretmanager:2.29.0'
```

Only the providers you include will be activated at runtime.

## Testing

To run the unit tests:
```bash
./gradlew test
```
Ensure the `starter-ksm-config.json` file located in `src/test/resources` is present. This file contains sanitized sample values and is provided only as a test fixture, not production credentials. You can override the path with:
```properties
keeper.ksm.secret-path=path/to/your-config.json
```
