package org.voyager.tests.config;

import lombok.Getter;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class FunctionalTestConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalTestConfig.class);
    @Getter
    private static final String baseUrl;
    @Getter
    private static final String authToken;
    @Getter
    private static final AirportsConfig airportsConfig;
    @Getter
    private static final HealthConfig healthConfig;

    static {
        Properties properties = loadPropertiesFile("config.properties");
        loadSystemEnvVariables(properties);

        baseUrl = properties.getProperty("voyager.url");
        authToken = properties.getProperty("voyager.auth.token");
        airportsConfig = new AirportsConfig(properties);
        healthConfig = new HealthConfig(properties);
    }

    private static Properties loadPropertiesFile(String filename) {
        Properties properties = new Properties();
        try (InputStream input = FunctionalTestConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
            } else {
                LOGGER.info("Warning: Profile-specific file not found: " + filename);
            }
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from: " + filename, e);
        }
    }

    private static void loadSystemEnvVariables(Properties properties) {
        properties.forEach((key,value) -> {
            LOGGER.debug(String.format("properties key: '%s', value: '%s'", key, value));
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
            pattern.matcher((String)value)
                    .results().map(matchResult -> matchResult.group(1))
                    .forEach(variable -> {
                        String actualValue = System.getenv(variable);
                        if (StringUtils.isBlank(actualValue)) {
                            throw new RuntimeException(String.format("Required system environment variable '%s' is invalid",variable));
                        }
                        properties.put(key,actualValue);
                        LOGGER.info(String.format("loaded system env var: '%s', value: '%s'",
                                variable, actualValue));
                    });
        });
    }
}
