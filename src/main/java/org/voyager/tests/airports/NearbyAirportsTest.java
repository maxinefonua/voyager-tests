package org.voyager.tests.airports;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.tests.config.FunctionalTestConfig;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportType;
import org.voyager.utils.ConstantsUtils;

public class NearbyAirportsTest {
    private static RequestSpecification requestSpec;
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUrl();
        RestAssured.basePath = FunctionalTestConfig.getAirportsConfig().getNearbyAirportsPath();
        String authToken = FunctionalTestConfig.getAuthToken();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(ConstantsUtils.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetNearbyAirports() {
        Double latitudeOfSLC = 40.7695;
        Double longitudeOfSLC = -111.8912;
        RestAssured.given()
                .spec(requestSpec)
                .queryParams(ConstantsUtils.LATITUDE_PARAM_NAME,latitudeOfSLC,
                        ConstantsUtils.LONGITUDE_PARAM_NAME,longitudeOfSLC,
                        ConstantsUtils.LIMIT_PARAM_NAME,3)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3))
                .body("[0].iata", Matchers.equalTo("SLC"))
                .body("[1].iata", Matchers.equalTo("BTF"))
                .body("[2].iata", Matchers.equalTo("HIF"));
    }

    @Test
    public void testGetNearbyAirportsWithFilters() {
        Double latitudeOfSLC = 40.7695;
        Double longitudeOfSLC = -111.8912;
        RestAssured.given()
                .spec(requestSpec)
                .queryParams(ConstantsUtils.LATITUDE_PARAM_NAME,latitudeOfSLC,
                        ConstantsUtils.LONGITUDE_PARAM_NAME,longitudeOfSLC,
                        ConstantsUtils.LIMIT_PARAM_NAME,3,
                        ConstantsUtils.TYPE_PARAM_NAME, AirportType.CIVIL.name(),
                        ConstantsUtils.AIRLINE_PARAM_NAME, Airline.UNITED)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3))
                .body("[0].iata", Matchers.equalTo("SLC"))
                .body("[1].iata", Matchers.equalTo("RKS"))
                .body("[2].iata", Matchers.equalTo("IDA"));
    }
}
