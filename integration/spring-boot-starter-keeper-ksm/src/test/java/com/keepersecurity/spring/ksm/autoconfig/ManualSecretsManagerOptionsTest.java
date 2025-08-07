package com.keepersecurity.spring.ksm.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ManualSecretsManagerOptionsTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withUserConfiguration(KeeperKsmAutoConfiguration.class)
          .withPropertyValues("keeper.ksm.secret-path=src/test/resources/starter-ksm-config.json");

  @Test
  void usesProvidedSecretsManagerOptions() {
    SecretsManagerOptions custom = new SecretsManagerOptions(new InMemoryStorage("{}"));
    contextRunner
        .withBean("secretsManagerOptions", SecretsManagerOptions.class, () -> custom)
        .run(
            context -> {
              assertThat(context).hasSingleBean(SecretsManagerOptions.class);
              assertThat(context.getBean(SecretsManagerOptions.class)).isSameAs(custom);
            });
  }
}
