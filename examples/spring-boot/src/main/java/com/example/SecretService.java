package com.example;

import com.keepersecurity.secretsManager.core.SecretsManager;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that fetches secrets from Keeper Secrets Manager and exposes
 * the resolved Spring configuration.
 */
@Service
public class SecretService {

    private static final Logger logger = LoggerFactory.getLogger(SecretService.class);

    private final SecretsManagerOptions options;
    private final Environment environment;

    /**
     * Creates a new service for retrieving secrets.
     *
     * @param options      configuration for the Secrets Manager client
     * @param environment  Spring environment to read configuration from
     */
    public SecretService(SecretsManagerOptions options, Environment environment) {
        this.options = options;
        this.environment = environment;
    }

    /**
     * Returns the first secret value that matches the given Keeper notation.
     *
     * @param keeperNotation notation path identifying the secret
     * @return the secret value or {@code null} if not found
     * @throws SecretFetchException if retrieval fails
     */
    public String fetchSecret(String keeperNotation) {
        if (keeperNotation == null || keeperNotation.isBlank()) {
            return null;
        }
        try {
            List<String> results = SecretsManager.getNotationResults(options, keeperNotation);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.error("Failed to fetch secret for notation {}", keeperNotation, e);
            throw new SecretFetchException("Failed to fetch secret", e);
        }
    }

    /**
     * Returns a map of all resolved Spring configuration properties.
     *
     * @return property name to value mappings
     */
    public Map<String, Object> getSpringConfig() {
        Map<String, Object> props = new LinkedHashMap<>();
        if (environment instanceof ConfigurableEnvironment configurableEnvironment) {
            MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
            for (PropertySource<?> propertySource : propertySources) {
                if (propertySource instanceof EnumerablePropertySource<?> enumerable) {
                    for (String name : enumerable.getPropertyNames()) {
                        props.put(name, enumerable.getProperty(name));
                    }
                }
            }
        }
        return props;
    }
}
