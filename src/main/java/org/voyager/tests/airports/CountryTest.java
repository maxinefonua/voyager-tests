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

public class CountryTest {
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
    public void getCountries() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", Matchers.hasItem("Yemen"));
    }

    @Test
    public void getCountriesWithContinent() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.CONTINENT_PARAM_NAME,"zz")
                .get(Path.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'continent' with value 'zz'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.CONTINENT_PARAM_NAME,"OC")
                .get(Path.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", Matchers.not(Matchers.hasItem("Yemen")))
                .body("name", Matchers.hasItem("Tonga"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.CONTINENT_PARAM_NAME,"OC","AS")
                .get(Path.COUNTRIES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", Matchers.hasItem("Yemen"))
                .body("name", Matchers.hasItem("Tonga"));
    }



    @Test
    public void getCountryByCode() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .pathParams(ParameterNames.COUNTRY_CODE_PARAM_NAME,"zz")
                .get(Path.COUNTRIES.concat(Path.BY_COUNTRY_CODE))
                .then()
                .assertThat()
                .statusCode(404)
                .body("message", Matchers.containsString("Invalid path variable 'countryCode' with value 'zz'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .pathParams(ParameterNames.COUNTRY_CODE_PARAM_NAME,"FI")
                .get(Path.COUNTRIES.concat(Path.BY_COUNTRY_CODE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", Matchers.equalTo("Finland"));
    }
}
