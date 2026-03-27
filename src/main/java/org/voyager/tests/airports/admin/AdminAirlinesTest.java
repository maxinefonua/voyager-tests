package org.voyager.tests.airports.admin;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
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
                .statusCode(405);
    }

    @Test
    public void deactivateAirline() {
        Airline airlineToDeactivate = Airline.VOLARIS;

        // get current airline airports
        List<String> airportCodes = getCurrentAirlineAirports(airlineToDeactivate);

        // test bad requests
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .post(Path.Admin.AIRLINES.concat(Path.Admin.DEACTIVATE))
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Missing required request parameter 'airline'"));

        // deactivate airline
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .queryParam(ParameterNames.AIRLINE,airlineToDeactivate.name())
                .when()
                .post(Path.Admin.AIRLINES.concat(Path.Admin.DEACTIVATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.equalTo(airportCodes.size()));

        // confirm airports do not return deactivated airline
        firstAndLastAirportContainsAirline(false,airlineToDeactivate,airportCodes);

        // reactivate airline airports
        AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder()
                .airline(airlineToDeactivate.name())
                .isActive(true)
                .iataList(airportCodes)
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
                .body("updatedCount", Matchers.equalTo(airportCodes.size()));

        // confirm airports return airline
        firstAndLastAirportContainsAirline(true,airlineToDeactivate,airportCodes);
    }

    private void firstAndLastAirportContainsAirline(boolean contains, Airline airlineToDeactivate, List<String> airportCodes) {
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .param(ParameterNames.IATA,airportCodes.get(0))
                .when()
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", contains ?
                        Matchers.hasItem(airlineToDeactivate.name()) :
                        Matchers.not(Matchers.hasItem(airlineToDeactivate.name())));

        int last = airportCodes.size()-1;
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .param(ParameterNames.IATA,airportCodes.get(last))
                .when()
                .get(Path.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", contains ?
                        Matchers.hasItem(airlineToDeactivate.name()) :
                        Matchers.not(Matchers.hasItem(airlineToDeactivate.name())));

    }

    private List<String> getCurrentAirlineAirports(Airline airlineToDeactivate) {
        return RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .param(ParameterNames.AIRLINE,airlineToDeactivate.name())
                .get(Path.IATA)
                .then()
                .statusCode(200)
                .body("",Matchers.not(Matchers.empty()))
                .extract()
                .jsonPath()
                .getList("", String.class);
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
}
