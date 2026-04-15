package org.voyager.tests.airports.admin;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.tests.config.FunctionalTestConfig;
import java.time.ZoneOffset;

public class AdminAirportsTest {
    private static RequestSpecification requestSpec;
    private static RequestSpecification adminRequestSpec;
    private static RequestSpecification testRequestSpec;
    private static final String TEST_IATA = "ZZZ";

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

        String testToken = FunctionalTestConfig.getTestAuthToken();
        testRequestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(Headers.AUTH_TOKEN_HEADER_NAME, testToken)
                .build();
        if (testAirportExists()) {
            deleteTestAirport();
        }
    }

    public static boolean testAirportExists() {
        Response getResponse = RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .pathParams(ParameterNames.IATA,TEST_IATA)
                .get(Path.AIRPORT_BY_IATA);
        return getResponse.statusCode() == 200;
    }

    public static void deleteTestAirport() {
        RestAssured.given()
                .spec(testRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .pathParams(ParameterNames.IATA, TEST_IATA)
                .delete(Path.Admin.AIRPORTS.concat(Path.BY_IATA))
                .then()
                .assertThat()
                .statusCode(204);
    }

    @AfterAll
    public static void cleanup() {
        deleteTestAirport();
    }

    @Test
    public void authentication() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .post(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(405);

        AirportForm airportForm = AirportForm.builder().build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportForm)
                .when()
                .post(Path.Admin.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(403)
                .body("error", Matchers.equalTo("Forbidden"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportForm)
                .when()
                .post(Path.Admin.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportForm)
                .when()
                .delete(Path.Admin.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(405)
                .body("message", Matchers.equalTo("Request method 'DELETE' is not supported"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportForm)
                .when()
                .pathParams(ParameterNames.IATA,"SJC")
                .delete(Path.Admin.AIRPORTS.concat(Path.BY_IATA))
                .then()
                .assertThat()
                .statusCode(403)
                .body("message", Matchers.equalTo("Forbidden"));

        Response response = RestAssured.given()
                .spec(testRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportForm)
                .when()
                .pathParams(ParameterNames.IATA, TEST_IATA)
                .delete(Path.Admin.AIRPORTS.concat(Path.BY_IATA));
        assert response.statusCode() != 403;
    }

    @Test
    public void addAndDeleteAirport() {
        AirportForm airportForm = AirportForm.builder()
                .iata(TEST_IATA)
                .countryCode("PR")
                .zoneId(ZoneOffset.UTC.getId())
                .airportType(AirportType.CIVIL.name())
                .latitude("10")
                .longitude("1")
                .name("Test Name")
                .city("Test City")
                .subdivision("Test Subdivision")
                .build();

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportForm)
                .when()
                .post(Path.Admin.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("city", Matchers.equalTo("Test City"));

        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .when()
                .pathParams(ParameterNames.IATA,TEST_IATA)
                .get(Path.AIRPORT_BY_IATA)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", Matchers.equalTo("Test Name"));
    }

    @Test
    public void patchAirport() {
        AirportPatch airportPatch = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch)
                .when()
                .pathParams(ParameterNames.IATA,"SJC")
                .patch(Path.Admin.AIRPORTS.concat(Path.BY_IATA))
                .then()
                .assertThat()
                .statusCode(403)
                .body("error", Matchers.equalTo("Forbidden"));

        AirportPatch airportPatch1 = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
        AirportPatch airportPatch2 = AirportPatch.builder().type(AirportType.CIVIL.name()).build();
        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch1)
                .when()
                .pathParams(ParameterNames.IATA,"SJC")
                .patch(Path.Admin.AIRPORTS.concat(Path.BY_IATA))
                .then()
                .assertThat()
                .statusCode(200)
                .body("type", Matchers.equalTo(AirportType.UNVERIFIED.name()));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch2)
                .when()
                .pathParams(ParameterNames.IATA,"SJC")
                .patch(Path.Admin.AIRPORTS.concat(Path.BY_IATA))
                .then()
                .assertThat()
                .statusCode(200)
                .body("type", Matchers.equalTo(AirportType.CIVIL.name()));
    }
}
