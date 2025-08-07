# Using an HSM to Store KSM Configuration

This guide shows how to configure a Spring Boot application that retrieves its Keeper Secrets Manager (KSM) configuration from a Hardware Security Module (HSM). Storing the KSM config in an HSM keeps the configuration material protected while still allowing the application to load it at runtime.

## Prerequisites

- A Keeper account with a generated KSM configuration (JSON)
- Access to an HSM (such as AWS CloudHSM, Thales Luna, or similar) and vendor tooling
- A Spring Boot project using the Keeper KSM starter
- Vendor libraries or drivers that allow your application to communicate with the HSM

## Export the KSM Configuration

1. Generate the KSM configuration using the Keeper CLI or Web Vault.
2. Save the resulting JSON to a secure machine that can communicate with the HSM.

## Load the Configuration into the HSM

1. Initialize a partition or slot on the HSM if needed.
2. Use the vendor's command line tool or API to create a new secret object.
3. Store the KSM configuration JSON in the secret object and note the identifier (for example, `ksm-config`).

## Configure the Spring Boot Application

1. Add the HSM vendor dependency to your `pom.xml` or build file.
2. Create a bean that reads the KSM configuration from the HSM and returns it to the KSM starter:

```java
@Bean
public KsmConfig ksmConfig(HsmClient hsmClient) {
    String json = hsmClient.readSecret("ksm-config");
    return KsmConfig.fromJson(json);
}
```

3. Ensure the application can authenticate to the HSM (for example, by using a smart card, network credentials, or environment variables).

## Run the Application

Start the Spring Boot application. During initialization the KSM starter will invoke the `ksmConfig` bean, retrieve the configuration from the HSM, and initialize the Keeper SDK.

Verify that the application can successfully retrieve secrets from Keeper. Monitor the HSM logs to confirm that the configuration was read only during startup.

## Maintenance Tips

- Rotate the KSM configuration in the HSM when credentials change.
- Restrict access to the HSM object so only the application can read it.
- Backup the KSM configuration securely in case the HSM needs to be re-initialized.

