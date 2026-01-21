package org.voyager.tests.airports.admin;

import groovyjarjarasm.asm.TypeReference;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.tests.config.FunctionalTestConfig;

import java.util.List;

class AdminAirlinesTest {
    private static RequestSpecification requestSpec;
    private static RequestSpecification adminRequestSpec;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUri();
        String authToken = FunctionalTestConfig.getUserAuthToken();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(Headers.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();

        String adminToken = FunctionalTestConfig.getAdminAuthToken();
        adminRequestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(Headers.AUTH_TOKEN_HEADER_NAME, adminToken)
                .build();
    }

    @Test
    public void authentication() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(403);

        RestAssured.given()
                .spec(adminRequestSpec)
                .when()
                .get(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(405);

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(403);

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400);

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(403);

        RestAssured.given()
                .spec(adminRequestSpec)
                .when()
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void batchUpsertAirline() {
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("Required request body missing"));
        AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().build();
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airlineBatchUpsert)
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"))
                .body("message", Matchers.containsString("airline"))
                .body("message", Matchers.containsString("isActive"))
                .body("message", Matchers.containsString("iataList"));

        airlineBatchUpsert = AirlineBatchUpsert.builder()
                .airline(Airline.DELTA.name())
                .isActive(true)
                .iataList(List.of())
                .build();

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airlineBatchUpsert)
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("iataList"));

        airlineBatchUpsert.setIataList(List.of("SJC","SFO"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airlineBatchUpsert)
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("skippedCount", Matchers.equalTo(2));
    }

    @Test
    public void batchDeleteAirline() {
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Required request parameter"))
                .body("message", Matchers.containsString("airline"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .queryParam(ParameterNames.AIRLINE, "")
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Missing required request parameter 'airline'"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .queryParam(ParameterNames.AIRLINE, "fakeairline")
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'airline' with value 'fakeairline'"));

        Airline airline = Airline.ZIPAIR;
        Response response = RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .queryParam(ParameterNames.AIRLINE, airline)
                .get(Path.IATA);

        List<String> airlineAirportList = response.body().as(new TypeRef<>() {});
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .queryParam(ParameterNames.AIRLINE, airline)
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.equalTo(airlineAirportList.size()));

        if (!airlineAirportList.isEmpty()) {
            AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder()
                    .iataList(airlineAirportList)
                    .isActive(true)
                    .airline(airline.name())
                    .build();

            RestAssured.given()
                    .spec(adminRequestSpec)
                    .contentType(ContentType.JSON)
                    .body(airlineBatchUpsert)
                    .when()
                    .post(Path.Admin.AIRLINES)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body("", Matchers.equalTo(airlineAirportList.size()));
        }
    }
}
