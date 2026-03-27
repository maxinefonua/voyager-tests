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
    private static final String baseUri;
    @Getter
    private static final String userAuthToken;
    @Getter
    private static final String adminAuthToken;
    @Getter
    private static final String testAuthToken;
    @Getter
    private static Properties properties;

    static {
        properties = loadPropertiesFile("config.properties");
        loadSystemEnvVariables(properties);
        baseUri = properties.getProperty("voyager.url");
        userAuthToken = properties.getProperty("voyager.auth.token");
        adminAuthToken = properties.getProperty("voyager.admin.token");
        testAuthToken = properties.getProperty("voyager.test.token");
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
