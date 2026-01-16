package org.voyager.tests.config;

import lombok.Getter;

public class HealthConfig {
    @Getter
    private static String healthPath;

    static {
        healthPath = FunctionalTestConfig.getProperties().getProperty("voyager.path.health");
    }
}
