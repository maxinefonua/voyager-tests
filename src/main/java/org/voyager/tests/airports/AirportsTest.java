package org.voyager.tests.airports;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.tests.config.FunctionalTestConfig;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.utils.ConstantsUtils;

public class AirportsTest {
    private static RequestSpecification requestSpec;
    @BeforeEach
    public void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUrl();
        RestAssured.basePath = FunctionalTestConfig.getAirportsConfig().getAirportsPath();
        String authToken = FunctionalTestConfig.getAuthToken();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(ConstantsUtils.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetIataCodes() {
        RestAssured.given()
                .spec(requestSpec)
                .basePath(FunctionalTestConfig.getAirportsConfig().getIataPath())
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // Assert the response is a non-empty array
                .body("$", Matchers.hasItem("HNL"));
    }

    @Test
    public void testPostIata() {
        RestAssured.given()
                .spec(requestSpec)
                .basePath(FunctionalTestConfig.getAirportsConfig().getIataPath())
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(405);
    }

    @Test
    public void testGetAirports() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0)) // Assert the response is a non-empty array
                .body("iata", Matchers.hasItem("HNL"));
    }

    @Test
    public void testGetAirport() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get("/ITM")
                .then()
                .assertThat()
                .statusCode(200)
                .body("subdivision", Matchers.equalTo("Hyogo"));
    }

    @Test
    public void testPatchAirport() {
        AirportPatch airportPatch1 = AirportPatch.builder().type(AirportType.UNVERIFIED.name()).build();
        AirportPatch airportPatch2 = AirportPatch.builder().type(AirportType.CIVIL.name()).build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch1)
                .when()
                .patch("/SJC")
                .then()
                .assertThat()
                .statusCode(200)
                .body("type", Matchers.equalTo(AirportType.UNVERIFIED.name()));

        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(airportPatch2)
                .when()
                .patch("/SJC")
                .then()
                .assertThat()
                .statusCode(200)
                .body("type", Matchers.equalTo(AirportType.CIVIL.name()));
    }
}
