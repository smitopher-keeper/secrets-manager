# Using SoftHSM2 with the HSM Provider

SoftHSM2 is a software implementation of a PKCS#11 compatible hardware security module. It is useful for development and testing the `HSM` provider profile in the Keeper Secrets Manager Spring Boot starter.

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

## Configure the provider
1. Ensure the `SOFTHSM2_CONF` environment variable points to your configuration file if you are not using the default location.
2. Configure the Spring Boot starter to use the PKCS#11 library. Depending on your installation, the library may reside in `/usr/lib/softhsm` or `/usr/local/lib/softhsm`:
   ```yaml
   keeper.ksm.pkcs11.library: /usr/lib/softhsm/libsofthsm2.so # or /usr/local/lib/softhsm/libsofthsm2.so
   ```
3. Start the application and enter the PIN when prompted.

SoftHSM2 should only be used for local development. For production deployments use a hardware security module that meets your security requirements.

