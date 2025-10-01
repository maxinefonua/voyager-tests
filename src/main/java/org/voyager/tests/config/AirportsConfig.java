package org.voyager.tests.config;

import lombok.Getter;
import java.util.Properties;

public class AirportsConfig {
    private static String airportsPath;
    private static String iataPath;
    private static String nearbyAirportsPath;
    private static String airportAirlinesPath;

    AirportsConfig(Properties properties) {
        airportsPath = properties.getProperty("voyager.path.airports");
        nearbyAirportsPath = properties.getProperty("voyager.path.nearby-airports");
        airportAirlinesPath = properties.getProperty("voyager.path.airport-airlines");
        iataPath = properties.getProperty("voyager.path.iata");
    }

    public String getAirportsPath() {
        return airportsPath;
    }

    public String getIataPath() {
        return iataPath;
    }

    public String getNearbyAirportsPath() {
        return nearbyAirportsPath;
    }

    public String getAirportAirlinesPath() {
        return airportAirlinesPath;
    }
}
