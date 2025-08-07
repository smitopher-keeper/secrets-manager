# Using SoftHSM2

SoftHSM2 is a software implementation of a PKCS#11 compatible hardware security module. It is useful for development and testing the `SOFTHSM2` provider profile in the Keeper Secrets Manager Spring Boot starter.

## Install SoftHSM2
- **Ubuntu**
  ```bash
  sudo apt-get install softhsm2
  ```
- **macOS**
  ```bash
  brew install softhsm
  ```

## Initialize a token
Create a token and set a PIN:
```bash
softhsm2-util --init-token --slot 0 --label ksm-token
```

Make note of the slot number (`0` in this example), the token label (`ksm-token`), and the PIN you chose. These values are needed when configuring your application.

## Configure the provider
1. Ensure the `SOFTHSM2_CONF` environment variable points to your configuration file if you are not using the default location.
2. Add the token details to your Spring configuration:
   ```yaml
   keeper:
     ksm:
       provider-type: softhsm2
       secret-path: pkcs11://slot/0/token/ksm-token   # use your slot and label
       secret-password: <PIN>                         # token PIN
   ```
3. Start the application and enter the PIN when prompted.

SoftHSM2 should only be used for local development. For production deployments use a hardware security module that meets your security requirements.

