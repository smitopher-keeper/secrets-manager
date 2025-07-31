package com.keepersecurity.spring.ksm.autoconfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Keeper Secrets Manager (KSM).
 * <p>
 * Prefix = "keeper.ksm". These properties can be set in application.properties or YAML.
 */
@ConfigurationProperties(prefix = "keeper.ksm")
public class KeeperKsmProperties implements InitializingBean{

  private static final Logger LOGGER = LoggerFactory.getLogger(KeeperKsmProperties.class);

  /**
   * Path to the secret container that holds the Keeper Secrets Manager configuration JSON. This can
   * be a file path or a location inside a secrets service.
   * <p>
   * If a one-time token is provided and this path is set, the token will be redeemed and the config
   * stored at this location. If not set, {@link KsmConfigProvider#FILE} is used and a token is provided, a default file "ksm-config.json"
   * will be used in the secretPath property.
   */
  private Path secretPath;
  /**
   * Path to a file containing a One-Time Access Token for Keeper Secrets Manager initialization.
   * <p>
   * The starter reads the token from this file, redeems it to generate the configuration and then deletes the file.
   * After a successful run the application terminates and you should remove this property from your configuration.
   */
  private Path oneTimeToken;

  /**
   * Fully qualified class name of a JCA security provider to register. Optional. If not set, a
   * provider will be chosen automatically.
   */
  private Class<? extends Provider> providerClass;

  /**
   * Type of secret container to use. Supported values are
   * {@code default}, {@code named}, {@code bc_fips},
   * {@code oracle_fips}, {@code sun_pkcs11}, {@code aws},
   * {@code azure}, {@code aws_hsm}, {@code azure_hsm},
   * Defaults to {@code default}.
   */
  private KsmConfigProvider providerType = KsmConfigProvider.DEFAULT;

  /**
   * User name for accessing the secret container. Defaults to "changeme".
   */
  private String secretUser = "changeme";

  /**
   * Password for accessing the secret container. Defaults to "changeme".
   */
  private String secretPassword = "changeme";

  /**
   * Path to the PKCS#11 library when using the "pkcs11" container type.
   */
  private String pkcs11Library;

  /**
   * When true, the application will fail to start unless the IL-5 certified provider is
   * available. This can be used to enforce IL-5 compliance.
   */
  private boolean enforceIl5 = false;

  // Getters and setters for the properties
  public Path getSecretPath() {
    return secretPath;
  }

  public void setSecretPath(Path secretPath) {
    this.secretPath = secretPath;
  }

  public Path getOneTimeToken() {
    return oneTimeToken;
  }

  public void setOneTimeToken(Path oneTimeToken) {
    this.oneTimeToken = oneTimeToken;
  }

  public Class<? extends Provider> getProviderClass() {
    return providerClass;
  }

  public void setProvider(Class<? extends Provider> providerClass) {
    this.providerClass = providerClass;
  }

  public KsmConfigProvider getProviderType() {
    return providerType;
  }

  public void setContainerType(KsmConfigProvider providerType) {
    this.providerType = providerType;
  }

  public String getSecretUser() {
    return secretUser;
  }

  public void setSecretUser(String secretUser) {
    this.secretUser = secretUser;
  }

  public String getSecretPassword() {
    return secretPassword;
  }

  public void setSecretPassword(String secretPassword) {
    this.secretPassword = secretPassword;
  }

  public String getPkcs11Library() {
    return pkcs11Library;
  }

  public void setPkcs11Library(String pkcs11Library) {
    this.pkcs11Library = pkcs11Library;
  }

  public boolean isEnforceIl5() {
    return enforceIl5;
  }

  public void setEnforceIl5(boolean enforceIl5) {
    this.enforceIl5 = enforceIl5;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (enforceIl5 && !providerType.isIl5Ready()) {
      notIl5Compliant(providerType);
    }
    if (secretPath == null) {
      secretPath = Paths.get(providerType.getDefaultLocation());
    }
  }

  private void notIl5Compliant(KsmConfigProvider configProvider) {
    String message = "%s is not IL-5 compliant".formatted(configProvider);
    LOGGER.atError().log(message);
    throw new IllegalStateException(message);
  }

}
