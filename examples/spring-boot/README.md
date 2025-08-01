# Keeper Secrets Manager Spring Boot Example

This sample demonstrates how to use the `spring-boot-starter-keeper-ksm` starter
in a minimal Spring Boot application.

## Prerequisites

- Java 21
- Gradle
- A Keeper Secrets Manager configuration file generated from a one-time token.

## Build and Run

To run the application:

```bash
./gradlew bootRun
```

The `application.yaml` file configures where the starter should look for your
Keeper Secrets Manager configuration and which records to load:

```yaml
keeper:
  ksm:
    secret-path: path/to/ksm-config.json
    records:
      - RECORD_UID
```
