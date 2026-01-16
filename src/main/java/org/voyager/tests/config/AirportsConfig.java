package org.voyager.tests.config;

import lombok.Getter;

import java.util.Properties;

public class AirportsConfig {
    @Getter
    private static String airportsPath;
    @Getter
    private static String adminAirportsPath;
    @Getter
    private static String iataPath;
    @Getter
    private static String nearbyAirportsPath;

    static {
        Properties properties = FunctionalTestConfig.getProperties();
        adminAirportsPath = properties.getProperty("voyager.admin.airports");
        airportsPath = properties.getProperty("voyager.path.airports");
        nearbyAirportsPath = properties.getProperty("voyager.path.nearby-airports");
        iataPath = properties.getProperty("voyager.path.iata");
    }
}
