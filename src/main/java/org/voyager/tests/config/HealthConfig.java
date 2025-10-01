package org.voyager.tests.config;

import java.util.Properties;

public class HealthConfig {
    private static String healthPath;

    HealthConfig(Properties properties) {
        healthPath = properties.getProperty("voyager.path.health");
    }

    public String getHealthPath(){
        return healthPath;
    }
}
