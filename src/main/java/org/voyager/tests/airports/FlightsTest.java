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
import org.voyager.commons.model.airline.Airline;
import org.voyager.tests.config.FunctionalTestConfig;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FlightsTest {
    private static RequestSpecification requestSpec;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUri();
        String authToken = FunctionalTestConfig.getUserAuthToken();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(Headers.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void getFlights() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("content", Matchers.not(Matchers.empty()));
    }

    @Test
    public void getFlightsInactive() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.IS_ACTIVE,false)
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100));
    }

    @Test
    public void getFlightsAirline() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.AIRLINE, Airline.DELTA.name())
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("content", Matchers.not(Matchers.empty()))
                .body("content.airline", Matchers.everyItem(Matchers.allOf(
                        Matchers.equalTo(Airline.DELTA.name()),
                        Matchers.not(Matchers.equalTo(Airline.SOUTHWEST.name()))
                )));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.AIRLINE,
                        String.format("%s,%s",Airline.DELTA.name(),Airline.SOUTHWEST.name()))
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("content", Matchers.not(Matchers.empty()))
                .body("content.airline", Matchers.everyItem(Matchers
                        .either(Matchers.equalTo(Airline.DELTA.name()))
                        .or(Matchers.equalTo(Airline.SOUTHWEST.name()))
                ));
    }

    @Test
    public void getFlightsNumber() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.FLIGHT_NUMBER, "JL116")
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("content", Matchers.everyItem(Matchers.allOf(
                        Matchers.not(Matchers.empty()),
                        Matchers.hasEntry("airline", Airline.JAPAN.name()),
                        Matchers.hasEntry("flightNumber", "JL116")
                )));
    }

    @Test
    public void getFlightsRouteId() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.ROUTE_ID, "125")
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("content", Matchers.everyItem(Matchers.allOf(
                        Matchers.not(Matchers.empty()),
                        Matchers.hasEntry("routeId", 125)
                )));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.ROUTE_ID, "125,101")
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("content.routeId", Matchers.everyItem(Matchers
                                .either(Matchers.equalTo(101))
                                .or(Matchers.equalTo(125))
                ));
    }

    @Test
    public void getFlightsStartEnd() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.FLIGHT_NUMBER, "DL904")
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("totalElements",Matchers.equalTo(2))
                .body("content", Matchers.everyItem(Matchers.allOf(
                        Matchers.not(Matchers.empty()),
                        Matchers.hasEntry("airline", Airline.DELTA.name()),
                        Matchers.hasEntry("flightNumber", "DL904")
                )));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.FLIGHT_NUMBER, "DL904")
                .queryParam(ParameterNames.START, ZonedDateTime.now().minusDays(1).format(formatter))
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("totalElements",Matchers.equalTo(2))
                .body("content", Matchers.everyItem(Matchers.allOf(
                        Matchers.not(Matchers.empty()),
                        Matchers.hasEntry("airline", Airline.DELTA.name()),
                        Matchers.hasEntry("flightNumber", "DL904")
                )));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.FLIGHT_NUMBER, "DL904")
                .queryParam(ParameterNames.START, ZonedDateTime.now().minusDays(1).format(formatter))
                .queryParam(ParameterNames.END, ZonedDateTime.now().plusDays(1).format(formatter))
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("size", Matchers.equalTo(100))
                .body("totalElements",Matchers.greaterThan(2))
                .body("content", Matchers.everyItem(Matchers.allOf(
                        Matchers.not(Matchers.empty()),
                        Matchers.hasEntry("airline", Airline.DELTA.name()),
                        Matchers.hasEntry("flightNumber", "DL904")
                )));
    }

    @Test
    public void getFlightsPageSize() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.SIZE, "1000")
                .queryParam(ParameterNames.PAGE, "1")
                .get(Path.FLIGHTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("first", Matchers.equalTo(false))
                .body("page", Matchers.equalTo(1))
                .body("size", Matchers.equalTo(1000));
    }
}
