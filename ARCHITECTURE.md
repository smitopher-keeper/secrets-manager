# Architecture Overview

This repository contains the open source components of **Keeper Secrets Manager**. It is structured into several parts:

- **sdk** – language specific client libraries used to interact with Keeper Secrets Manager. Subdirectories include `python`, `java`, `javascript`, `.NET`, `golang`, and `rust`.
- **integration** – plugins and tools that integrate Keeper with external platforms such as Ansible, Azure DevOps, HashiCorp Vault, the command line interface and others.
- **examples** – small example applications demonstrating how to use the various SDKs.

Each module has its own README describing usage and build instructions. SDKs expose a common interface for retrieving and managing secrets. Integrations build on top of these libraries to make secrets available in CI/CD pipelines and other environments.
