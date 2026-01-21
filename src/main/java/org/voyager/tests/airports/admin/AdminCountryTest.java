package org.voyager.tests.airports.admin;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.tests.config.FunctionalTestConfig;
import java.util.List;

public class AdminCountryTest {
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
    public void authenticatePost() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .post(Path.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(405);

        CountryForm countryForm = CountryForm.builder().build();
        RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(countryForm)
                .when()
                .post(Path.Admin.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(403)
                .body("error", Matchers.equalTo("Forbidden"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(countryForm)
                .when()
                .post(Path.Admin.COUNTRIES)
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
                .delete(Path.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(405);

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .delete(Path.Admin.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(403)
                .body("error", Matchers.equalTo("Forbidden"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .when()
                .delete(Path.Admin.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(405);

        RestAssured.given()
                .spec(adminRequestSpec)
                .when()
                .pathParams(ParameterNames.COUNTRY_CODE,"ZZ")
                .delete(Path.Admin.COUNTRIES.concat(Path.BY_COUNTRY_CODE))
                .then()
                .assertThat()
                .statusCode(403);

        Response response = RestAssured.given()
                        .spec(testRequestSpec)
                        .when()
                        .pathParams(ParameterNames.COUNTRY_CODE,"ZZ")
                        .get(Path.COUNTRIES.concat(Path.BY_COUNTRY_CODE));
        if (response.statusCode() == 404) {
            RestAssured.given()
                    .spec(testRequestSpec)
                    .when()
                    .pathParams(ParameterNames.COUNTRY_CODE, "ZZ")
                    .delete(Path.Admin.COUNTRIES.concat(Path.BY_COUNTRY_CODE))
                    .then()
                    .assertThat()
                    .statusCode(404);
        }
    }

    @Test
    public void addAndCleanupCountry() {
        Response response = RestAssured.given()
                        .spec(requestSpec)
                        .when()
                        .pathParams(ParameterNames.COUNTRY_CODE,"zz")
                        .get(Path.COUNTRIES.concat(Path.BY_COUNTRY_CODE));
        if (response.statusCode() == 200) {
            RestAssured.given()
                    .spec(testRequestSpec)
                    .when()
                    .pathParams(ParameterNames.COUNTRY_CODE,"ZZ")
                    .delete(Path.Admin.COUNTRIES.concat(Path.BY_COUNTRY_CODE))
                    .then()
                    .assertThat()
                    .statusCode(204);
        }

        CountryForm countryForm = CountryForm.builder()
                .code("ZZ")
                .west(-10.0)
                .east(10.0)
                .north(10.0)
                .south(-10.0)
                .name("Test Name")
                .areaInSqKm(10.0)
                .capitalCity("Test Capital City")
                .population(100L)
                .currencyCode("TSD")
                .continent("TEST")
                .languages(List.of("en-US"))
                .build();

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(countryForm)
                .when()
                .post(Path.Admin.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"));

        countryForm.setContinent(Continent.OC.name());

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(countryForm)
                .when()
                .post(Path.Admin.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", Matchers.equalTo(countryForm.getName()));

        RestAssured.given()
                .spec(testRequestSpec)
                .when()
                .pathParams(ParameterNames.COUNTRY_CODE,"ZZ")
                .delete(Path.Admin.COUNTRIES.concat(Path.BY_COUNTRY_CODE))
                .then()
                .assertThat()
                .statusCode(204);
    }
}
