package utils.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class EnvironmentConfigLoader {
    private static final String DEFAULT_ENV = "dev";
    private static final String CONFIG_PATH_PREFIX = "config/";
    private static final String CONFIG_PATH_SUFFIX = ".properties";

    private EnvironmentConfigLoader() {
    }

    public static TestEnvironment load() {
        String envName = System.getProperty("dog.api.env", DEFAULT_ENV);
        if (envName == null || envName.isBlank()) {
            envName = DEFAULT_ENV;
        }
        String configPath = CONFIG_PATH_PREFIX + envName + CONFIG_PATH_SUFFIX;

        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(configPath)) {
            if (in == null) {
                throw new IllegalStateException("Não foi possível carregar config: " + configPath);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar config: " + configPath, e);
        }

        return new TestEnvironment(
                envName,
                required(properties, "dog.api.baseUri"),
                requiredInt(properties, "dog.api.timeout.connect.millis"),
                requiredInt(properties, "dog.api.timeout.read.millis"),
                requiredInt(properties, "dog.api.responseTime.max.millis"),
                required(properties, "dog.api.breed.sample"),
                requiredInt(properties, "dog.api.breed.sample.count")
        );
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Propriedade obrigatória ausente: " + key);
        }
        return value.trim();
    }

    private static int requiredInt(Properties properties, String key) {
        return Integer.parseInt(required(properties, key));
    }
}

