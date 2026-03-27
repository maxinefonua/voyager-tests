package org.voyager.tests.airports;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
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
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // assert non-empty
                .body("", Matchers.hasItem("JAPAN"));
    }

    @Test
    public void getAirlinesIataParams() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("iata","HNL","HLN")
                .queryParam("operator","AND")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.lessThan(5)) // assert non-empty
                .body("", Matchers.not(Matchers.hasItem("HAWAIIAN")));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("iata","HNL","HLN")
                .get(Path.AIRLINES)
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
                .queryParam("origin","ITM")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("destination"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("origin","ITM")
                .queryParam("destination","SMF")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.is(0));

        String destination2 = "destination=HNL";
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("origin","ITM")
                .queryParam("destination","HNL")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0) )
                .body("", Matchers.hasItem("JAPAN"));
    }

    @Test
    public void getAirlinesBothParams() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("origin","ITM")
                .queryParam("iata","HNL","HLN")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("both"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("iata","HNL","HLN")
                .queryParam("destination","SMF")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("both"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam("origin","ITM")
                .queryParam("iata","HNL","HLN")
                .queryParam("destination","SMF")
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",Matchers.containsString("both"));
    }

    @Test
    public void getAirlinesSingleAirport() {
        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ParameterNames.IATA, List.of("HEL"))
                .when()
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // assert non-empty
                .body("", Matchers.hasItem("FINNAIR"));
    }
}
