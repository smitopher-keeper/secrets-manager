# Keeper Secrets Manager Spring Boot Example

This sample demonstrates how to use the `spring-boot-starter-keeper-ksm` starter
in a minimal Spring Boot application.

## Keeper Spring Boot Example

This example demonstrates fetching KSM secrets using a Thymeleaf frontend.

### Prerequisites

- Java 21
- Gradle
- You must have a valid Keeper KSM config file stored at:
  `~/.keeper/ksm-config.json`
  - You can generate one using:
    `keeper shell ksm profile init --app-owner`
  - Or follow the Keeper KSM docs:
    https://docs.keeper.io/secrets-manager/

Edit `src/main/resources/application.yaml` to enable config loading:

```yaml
ksm:
  config:
    path: ~/.keeper/ksm-config.json
    useRawJson: true
```

### Start the example

From `/examples/spring-boot`, run:

```bash
./gradlew bootRun
```

Then open <http://localhost:8080> in your browser.

🔎 **Use the UI**

Paste a Keeper Notation string (e.g. `keeper://ABC123/field/password`)

Click "Fetch Secret"

The fetched value will appear, along with a list of Spring configuration
properties populated from KSM.
