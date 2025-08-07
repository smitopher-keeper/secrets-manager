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

- **Option 1: One-Time Token (OTT)** – Use this for first-time setup. Provide the token given by Keeper:

```properties
keeper.ksm.one-time-token = <Token value>
```
At startup the starter consumes the token , writes the retrieved KSM config to the configured container.

To avoid storing the token path in `application.properties`, supply it at runtime:
```bash
java -jar app.jar --keeper.ksm.one-time-token=/path/to/one-time-token.txt
```
or
```bash
export KEEPER_KSM_ONE_TIME_TOKEN=/path/to/one-time-token.txt
java -jar app.jar
```
After the configuration is generated, remove the `keeper.ksm.one-time-token` property or environment variable before restarting. One-time tokens can only be redeemed once.

When `keeper.ksm.enforce-il5=true`, this one-time-token bootstrapping is blocked to maintain IL5 compliance. You may override this behavior by setting `bootstrap.check.mode=warn` to merely log a warning.

IL5 enforcement also requires audit logging. Configure a logger named `com.keepersecurity` or `com.keepersecurity.ksm` at level `INFO` or higher and direct it to a secure sink such as a file appender. If no audit sink is detected the application fails to start by default. Override this behavior with `audit.check.mode=warn` to only log a warning.

- **Option 2: Existing Config File** – If you already have a Keeper config JSON (e.g., from a previous initialization), you can just specify:
  ```properties
  keeper.ksm.secret-path = path/to/ksm-config.json
  ```
  Ensure the file is accessible at runtime. The starter will load this file to configure access to Keeper Secrets Manager.

- **Option 3: Secret Container Options** – The config can be stored in different container providers. By default a local file is used. Set the provider type and optional credentials if needed:
  ```properties
  keeper.ksm.provider-type = bc_fips
  keeper.ksm.secret-user = changeme
  keeper.ksm.secret-password = changeme
  ```

### Supported Provider Types

The `keeper.ksm.provider-type` property accepts the following values.

| Value | Default location | Security level | Compliance profile | Notes
|-------|-----------------|----------------|--------------------|-------
| `default` | `ksm-config.p12` | IL-2 | None | default Java keystore
| `named`   | `ksm-config.p12` | IL-2 | None | named keystore entry
| `bc_fips` | `ksm-config.bcfks` | IL-5 | FIPS 140-2 | requires Bouncy Castle FIPS
| `oracle_fips` | `ksm-config.p12` | IL-5 | FIPS 140-2 | Oracle FIPS provider
| `aws`     | `aws-secrets://region/resource` | IL-5 | FedRAMP High | requires AWS SDK
| `azure`   | `azure-keyvault://vault/resource` | IL-5 | FedRAMP High | requires Azure SDK
| `google`  | `gcp-secretmanager://project/resource` | IL-4 | FedRAMP Moderate | requires Google SDK
| `raw`     | `ksm-config.json` | IL-2 | None | plain JSON file

**Caution:** The `raw` provider stores secrets in clear text and should only be used for testing or other non-production environments.

### IL-5 Provider Summary

The following providers from the
[`KsmConfigProvider`](src/main/java/com/keepersecurity/spring/ksm/autoconfig/KsmConfigProvider.java)
enum are IL-5 ready. Each entry complies with the corresponding security
profile indicated below:

| Provider | Enum constant | Compliance highlights
|----------|---------------|-----------------------
| Bouncy Castle FIPS Keystore | `BC_FIPS` | Utilizes the Bouncy Castle FIPS provider for FIPS 140-2 compliance.
| Oracle FIPS Keystore | `ORACLE_FIPS` | Oracle JCE FIPS provider for FIPS 140-2 compliance.
| AWS Secrets Manager | `AWS` | Cloud-based integration that meets FedRAMP High.
| Azure Key Vault | `AZURE` | Cloud-based integration that meets FedRAMP High.

All of the above return `true` from `isIl5Ready()` in `KsmConfigProvider`,
signaling that they meet IL-5 security requirements.

- **Key Store Placeholder** – You can store the configuration in a Java KeyStore instead of a plain file. Set the container type to `keystore` and optionally provide your own alias and password:
  ```properties
  keeper.ksm.provider-type = keystore
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
      provider-type: softhsm2
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
