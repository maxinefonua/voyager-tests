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

public class RoutesTest {
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
    public void getRoutes() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.ROUTES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.not(Matchers.empty()))
                .body("origin",Matchers.hasItem("YOW"));
    }

    @Test
    public void getRoutesWithOrigin() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.ORIGIN,"HNL")
                .get(Path.ROUTES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.not(Matchers.empty()))
                .body("destination",Matchers.hasItem("SFO"))
                .body("destination",Matchers.not(Matchers.hasItem("HEL")));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.ORIGIN,"HNL","KIX")
                .get(Path.ROUTES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.not(Matchers.empty()))
                .body("destination",Matchers.hasItem("SFO"))
                .body("destination",Matchers.hasItem("HEL"));
    }

    @Test
    public void getRoutesWithDestination() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.DESTINATION,"YYC")
                .get(Path.ROUTES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.not(Matchers.empty()))
                .body("origin",Matchers.hasItem("SFO"))
                .body("destination",Matchers.not(Matchers.hasItem("ABQ")));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.DESTINATION,"YYC","SEA")
                .get(Path.ROUTES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.not(Matchers.empty()))
                .body("origin",Matchers.hasItem("SFO"))
                .body("origin",Matchers.hasItem("ABQ"));
    }
}