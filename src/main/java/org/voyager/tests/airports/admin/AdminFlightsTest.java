package org.voyager.tests.airports.admin;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.flight.FlightUpsert;
import org.voyager.tests.config.FunctionalTestConfig;
import java.util.List;

public class AdminFlightsTest {
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
    public void authenticatePost() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .post(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(405);

        FlightBatchUpsert flightBatchUpsert = FlightBatchUpsert.builder().build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchUpsert)
                .when()
                .post(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(403)
                .body("error", Matchers.equalTo("Forbidden"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchUpsert)
                .when()
                .post(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"));
    }

    @Test
    public void authenticateDelete() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .delete(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(405);

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .delete(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(403)
                .body("error", Matchers.equalTo("Forbidden"));

        FlightBatchDelete flightBatchDelete = FlightBatchDelete.builder().build();
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchDelete)
                .when()
                .delete(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"));
    }

    @Test
    public void batchDeleteFlights() {
        FlightBatchDelete flightBatchDelete = FlightBatchDelete.builder()
                .daysPast("7")
                .build();
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchDelete)
                .when()
                .delete(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.either(Matchers.equalTo(0)).or(Matchers.greaterThan(0)));
    }

    @Test
    public void batchUpsert() {
        FlightBatchUpsert flightBatchUpsert = FlightBatchUpsert.builder()
                .flightUpsertList(List.of())
                .build();
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchUpsert)
                .when()
                .post(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("'flightUpsertList' must not be empty"));

        flightBatchUpsert.setFlightUpsertList(List.of(
                FlightUpsert.builder()
                        .build()
        ));
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchUpsert)
                .when()
                .post(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message",
                        Matchers.containsString(
                                "flightUpsertList[0].isArrival' must be a valid boolean string (true/false)"
                        )
                );
        flightBatchUpsert.setFlightUpsertList(List.of(
                FlightUpsert.builder()
                        .isArrival("true")
                        .flightNumber("TS101")
                        .build()
        ));
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(flightBatchUpsert)
                .when()
                .post(Path.Admin.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"));
    }
}
