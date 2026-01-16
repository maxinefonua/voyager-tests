package org.voyager.tests.airports;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.tests.config.AirlinesConfig;
import org.voyager.tests.config.FunctionalTestConfig;
import java.util.List;

public class AirlinesTest {
    private static RequestSpecification requestSpec;
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUri();
        String authToken = FunctionalTestConfig.getUserAuthToken();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(Headers.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetAirlines() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath())
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // assert non-empty
                .body("", Matchers.hasItem("JAPAN"));
    }

    @Test
    public void testGetAirlinesForAirport() {
        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ParameterNames.IATA_PARAM_NAME, List.of("HEL"))
                .when()
                .get(AirlinesConfig.getAirlinesPath())
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // assert non-empty
                .body("", Matchers.hasItem("FINNAIR"));
    }
}
