package com.example;

import com.keepersecurity.secretsManager.core.SecretsManager;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretServiceTest {

    @Mock
    SecretsManagerOptions options;
    @Mock
    Environment environment;

    @Test
    void fetchSecretReturnsValue() {
        try (MockedStatic<SecretsManager> secretsManager = mockStatic(SecretsManager.class)) {
            secretsManager.when(() -> SecretsManager.getNotationResults(options, "keeper://record/field"))
                    .thenReturn(List.of("value"));

            SecretService service = new SecretService(options, environment);
            String result = service.fetchSecret("keeper://record/field");

            assertThat(result).isEqualTo("value");
        }
    }

    @Test
    void fetchSecretReturnsNullForBlank() {
        SecretService service = new SecretService(options, environment);
        String result = service.fetchSecret("  ");
        assertThat(result).isNull();
    }

    @Test
    void fetchSecretThrowsOnError() {
        try (MockedStatic<SecretsManager> secretsManager = mockStatic(SecretsManager.class)) {
            secretsManager.when(() -> SecretsManager.getNotationResults(options, "keeper://bad"))
                    .thenThrow(new RuntimeException("boom"));

            SecretService service = new SecretService(options, environment);

            assertThatThrownBy(() -> service.fetchSecret("keeper://bad"))
                    .isInstanceOf(SecretFetchException.class)
                    .hasMessage("Failed to fetch secret");
        }
    }
}
