package org.voyager.tests.config;

import lombok.Getter;

import java.util.Properties;

public class AirlinesConfig {
    @Getter
    private static String airlinesPath;
    @Getter
    private static String adminAirlinesPath;

    static {
        Properties properties = FunctionalTestConfig.getProperties();
        airlinesPath = properties.getProperty("voyager.path.airport-airlines");
        adminAirlinesPath = properties.getProperty("voyager.admin.airport-airlines");
    }
}
