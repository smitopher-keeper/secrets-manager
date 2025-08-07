package com.example;

import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Example Spring Boot application bootstrap class. */
@SpringBootApplication
public class Application {

  /**
   * Launches the Spring Boot application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  KeyValueStorage ksmConfig(@Value("${keeper.ksm.secret-path}") String secretPath)
      throws IOException {
    String json = Files.readString(Path.of(secretPath));
    return new InMemoryStorage(json);
  }
}
