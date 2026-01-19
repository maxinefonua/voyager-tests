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
    public void getAirlines() {
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
    public void getAirlinesIataParams() {
        String params = "iata=HNL,HLN";
        String ops = "operator=AND";
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath()
                        .concat(String.format("?%s&%s",params,ops)))
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.lessThan(5)) // assert non-empty
                .body("", Matchers.not(Matchers.hasItem("HAWAIIAN")));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath().concat(String.format("?%s",params)))
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // assert non-empty
                .body("", Matchers.hasItem("HAWAIIAN"));
    }

    @Test
    public void getAirlinesPathParams() {
        String origin = "origin=ITM";
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath()
                        .concat(String.format("?%s",origin)))
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("destination"));

        String destination = "destination=SMF";
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath().concat(String.format("?%s&%s",origin,destination)))
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.is(0));

        String destination2 = "destination=HNL";
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath().concat(String.format("?%s&%s",origin,destination2)))
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0) )
                .body("", Matchers.hasItem("JAPAN"));
    }

    @Test
    public void getAirlinesBothParams() {
        String origin = "origin=ITM";
        String destination = "destination=SMF";
        String iata = "iata=HNL,HLN";
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath()
                        .concat(String.format("?%s&%s",origin,iata)))
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("both"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath()
                        .concat(String.format("?%s&%s",iata,destination)))
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("both"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(AirlinesConfig.getAirlinesPath()
                        .concat(String.format("?%s&%s&%s",iata,origin,destination)))
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("both"));
    }

    @Test
    public void getAirlinesSingleAirport() {
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
