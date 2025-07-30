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

- **Option 1: One-Time Token (OTT)** – Use this for first-time setup. Provide the one-time access token given by Keeper:
  ```properties
  keeper.ksm.one-time-token = YOUR_ONE_TIME_TOKEN_HERE
  keeper.ksm.secret-path = path/to/ksm-config.json
  ```
  On the first run, the starter will use the token to retrieve your KSM configuration and save it to the specified JSON file. **Note:** One-time tokens can only be used once; after the config file is created, remove the token from your configuration.

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
