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
On the first run, the starter will read the token from the file, retrieve your KSM configuration and save it to the specified JSON file. The token file is deleted after use. **Note:** One-time tokens can only be used once; after the config file is created, remove the token file from your system.

- **Option 2: Existing Config File** – If you already have a Keeper config JSON (e.g., from a previous initialization), you can just specify:
  ```properties
  keeper.ksm.secret-path = path/to/ksm-config.json
  ```
  Ensure the file is accessible at runtime. The starter will load this file to configure access to Keeper Secrets Manager.

- **Secret Container Options** – The config can be stored in different container types. By default a local file is used. Set the container type and optional credentials if needed:
  ```properties
  keeper.ksm.container-type = pkcs11
  keeper.ksm.pkcs11-library = /path/to/lib.so
  keeper.ksm.secret-user = changeme
  keeper.ksm.secret-password = changeme
  ```
When `container-type` is `pkcs11`, this starter uses `Pkcs11ConfigStorage`. This class only stores the Keeper configuration in memory and **does not** load the specified library as a real PKCS#11 provider. A full PKCS#11 integration would require additional implementation.

### Supported Container Types

The `keeper.ksm.container-type` property accepts the following values.  Options marked *not implemented* are reserved for future releases.

| Value | Default location | Security level | Compliance profile | Notes |
|-------|-----------------|----------------|--------------------|-------|
| `default` | `kms-config.p12` | IL-5 | FedRAMP High | default Java keystore |
| `named`   | `kms-config.p12` | IL-2 | None | named keystore entry |
| `bc_fips` | `kms-config.bcfks` | IL-5 | FIPS 140-2 | requires Bouncy Castle FIPS |
| `aws`     | `aws-secrets://region/resource` | IL-5 | FedRAMP High | *not implemented* |
| `azure`   | `azure-keyvault://vault/resource` | IL-5 | FedRAMP High | *not implemented* |
| `google`  | `gcp-secretmanager://project/resource` | IL-4 | FedRAMP Moderate | *not implemented* |
| `raw`     | `kms-config.json` | IL-2 | None | plain JSON file |
| `hsm`     | `pkcs11://slot/0/token/kms` | IL-5 | FIPS 140-2 | PKCS#11 HSM |

### IL-5 Provider Summary

The following providers from the
[`KsmConfigProvider`](src/main/java/com/keepersecurity/spring/ksm/autoconfig/KsmConfigProvider.java)
enum are IL-5 ready. Each entry complies with the corresponding security
profile indicated below:

| Provider | Enum constant | Compliance highlights |
|----------|---------------|-----------------------|
| Default Java Keystore | `DEFAULT` | FedRAMP High profile. Suitable for IL-5 workloads using the JVM keystore. |
| Bouncy Castle FIPS Keystore | `BC_FIPS` | Utilizes the Bouncy Castle FIPS provider for FIPS 140-2 compliance. |
| AWS Secrets Manager | `AWS` | Cloud-based integration that meets FedRAMP High. |
| Azure Key Vault | `AZURE` | Cloud-based integration that meets FedRAMP High. |
| Hardware Security Module | `HSM` | Stores configuration through PKCS#11, providing FIPS 140-2 compliance. |

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

If neither property is set, the auto-configuration will not initialize and will throw an error to remind you to configure KSM credentials.

## Testing

To run the unit tests:
```bash
./gradlew test
```
Ensure the `starter-ksm-config.json` file located in `src/test/resources` is present. This file contains sanitized sample values and is provided only as a test fixture, not production credentials. You can override the path with:
```properties
keeper.ksm.secret-path=path/to/your-config.json
```
