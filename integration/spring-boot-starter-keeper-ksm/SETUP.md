# Setup Guide

## Prerequisites
- JDK 21
- Gradle 8 or later
- Keeper Secrets Manager One-Time Token or existing config JSON

## Building the Project
```bash
./gradlew clean build
```

## Configuration

Copy the provided `src/test/resources/starter-ksm-config.json` test fixture to your desired location and set the `keeper.ksm.secret-path` property in your Spring Boot application:

```properties
keeper.ksm.secret-path=path/to/ksm-config.json
```

Alternatively, provide a path to a one-time token file to generate the config:

```properties
keeper.ksm.one-time-token=path/to/one-time-token.txt
keeper.ksm.secret-path=ksm-config.json
```

If you need to specify a custom security provider add:

```properties
keeper.ksm.provider=org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
```

To fail startup when the FIPS provider is not present, enable IL-5 enforcement:

```properties
keeper.ksm.enforce-il5=true
```

## Running Tests

```bash
./gradlew test
```
