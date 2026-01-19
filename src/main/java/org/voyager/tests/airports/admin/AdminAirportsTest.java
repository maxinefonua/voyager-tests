package org.voyager.tests.airports.admin;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
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
    }

    @Test
    public void addAirportAndDeleteAirport() {
        String iata = "ZZZ";
        Response getResponse = RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .pathParams(ParameterNames.IATA_PARAM_NAME,iata)
                .get(Path.AIRPORT_BY_IATA);
        if (getResponse.statusCode() != 200) {
            AirportForm airportForm = AirportForm.builder()
                    .iata(iata)
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
                    .pathParams(ParameterNames.IATA_PARAM_NAME,iata)
                    .get(Path.AIRPORT_BY_IATA)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body("name", Matchers.equalTo("Test Name"));
        }

        RestAssured.given()
                .spec(testRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .pathParams(ParameterNames.IATA_PARAM_NAME,iata)
                .delete("/admin".concat(Path.AIRPORT_BY_IATA))
                .then()
                .assertThat()
                .statusCode(204);
    }

    @Test
    public void patchAirport() {
        AirportPatch airportPatch = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch)
                .when()
                .pathParams(ParameterNames.IATA_PARAM_NAME,"SJC")
                .patch("/admin".concat(Path.AIRPORT_BY_IATA))
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
                .pathParams(ParameterNames.IATA_PARAM_NAME,"SJC")
                .patch("/admin".concat(Path.AIRPORT_BY_IATA))
                .then()
                .assertThat()
                .statusCode(200)
                .body("type", Matchers.equalTo(AirportType.UNVERIFIED.name()));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch2)
                .when()
                .pathParams(ParameterNames.IATA_PARAM_NAME,"SJC")
                .patch("/admin".concat(Path.AIRPORT_BY_IATA))
                .then()
                .assertThat()
                .statusCode(200)
                .body("type", Matchers.equalTo(AirportType.CIVIL.name()));
    }
}
