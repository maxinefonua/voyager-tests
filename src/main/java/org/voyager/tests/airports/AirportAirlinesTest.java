package org.voyager.tests.airports;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.tests.config.FunctionalTestConfig;
import org.voyager.utils.ConstantsUtils;
import java.util.List;

public class AirportAirlinesTest {
    private static RequestSpecification requestSpec;
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUrl();
        RestAssured.basePath = FunctionalTestConfig.getAirportsConfig().getAirportAirlinesPath();
        String authToken = FunctionalTestConfig.getAuthToken();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(ConstantsUtils.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetAirlines() {
        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ConstantsUtils.IATA_PARAM_NAME, List.of("HEL"))
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // Assert the response is a non-empty array
                .body("", Matchers.hasItem("FINNAIR"));
    }
}
