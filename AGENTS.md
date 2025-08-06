# Agent Instructions

This repository hosts the Keeper Secrets Manager Spring Boot starter
alongside other integrations.

The repository is organized into:

- `sdk/` contains SDK projects for dotnet, golang, java, javascript,
 python and rust.
- `integration/` contains integration projects for
 `keeper_secrets_manager_ansible`,
 `keeper_secrets_manager_azure_pipeline_extension`,
 `keeper_secrets_manager_cli`, `servicenow-external-credential-resolver`,
 `spring-boot-starter-keeper-ksm` and `vault-plugin-secrets-ksm`.
- `examples/` contains sample projects:
  - `dotNet` for the dotnet SDK
  - `go` for the golang SDK
  - `java` for the java SDK
  - `javascript` for the javascript SDK
  - `kotlin` for the java SDK
  - `python` for the python SDK
  - `spring-boot` for the `spring-boot-starter-keeper-ksm` integration

- Most development is done in `integration/spring-boot-starter-keeper-ksm`.
 That directory has its own `AGENTS.md` with detailed build instructions.
- Always consult and follow that file when working on the starter. It
 requires JDK 21 and running `./gradlew test` before committing.
- For other parts of the repo, keep documentation and configuration
 consistent with the existing style.

