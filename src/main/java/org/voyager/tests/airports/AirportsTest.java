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

public class AirportsTest {
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
    public void getIataCodes() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.IATA)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("", Matchers.hasItem("HNL"));
    }

    @Test
    public void getIataCodesType() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.TYPE,"faketype")
                .get(Path.IATA)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'type' with value 'faketype'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.TYPE,"unverified")
                .get(Path.IATA)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("", Matchers.not(Matchers.hasItem("HNL")))
                .body("", Matchers.hasItem("AMJ"));
    }

    @Test
    public void getIataCodesAirline() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.AIRLINE,"fakeairline")
                .get(Path.IATA)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'type' with value 'fakeairline'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.AIRLINE,"japan")
                .get(Path.IATA)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("", Matchers.not(Matchers.hasItem("SJC")))
                .body("", Matchers.hasItem("ITM"));
    }

    @Test
    public void getPagedAirports() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size", Matchers.equalTo(100))
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("last", Matchers.equalTo(false))
                .body("content.iata", Matchers.hasItem("ABQ"));


        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.SIZE,"0")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("'size' must be greater than or equal to 1"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.PAGE,"-1")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("'page' must be greater than or equal to 0"));
    }

    @Test
    public void getPagedAirportsType() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.TYPE,"faketype")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'type' with value 'faketype'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.TYPE,"military")
                .queryParam(ParameterNames.SIZE,"1000")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size", Matchers.equalTo(1000))
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("last", Matchers.equalTo(true))
                .body("content.iata", Matchers.not(Matchers.hasItem("ABQ")));
    }

    @Test
    public void getPagedAirportsCountry() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.COUNTRY_CODE,"12")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'countryCode' with value '12'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.COUNTRY_CODE,"to")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("content.iata", Matchers.not(Matchers.hasItem("ABQ")))
                .body("content.iata", Matchers.hasItem("EUA"))
                .body("size", Matchers.equalTo(100))
                .body("page", Matchers.equalTo(0))
                .body("last", Matchers.equalTo(true))
                .body("first", Matchers.equalTo(true))
                .body("numberOfElements", Matchers.greaterThan(0));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.COUNTRY_CODE,"to")
                .queryParam(ParameterNames.TYPE,"military")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("content.iata", Matchers.not(Matchers.hasItem("EUA")))
                .body("size", Matchers.equalTo(100))
                .body("page", Matchers.equalTo(0))
                .body("last", Matchers.equalTo(true))
                .body("first", Matchers.equalTo(true))
                .body("numberOfElements", Matchers.equalTo(0));
    }

    @Test
    public void getPagedAirportsAirline() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.AIRLINE,"fakeairline")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'airline' with value 'fakeairline'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.AIRLINE,"delta")
                .queryParam(ParameterNames.SIZE,"1000")
                .get(Path.AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size", Matchers.equalTo(1000))
                .body("page", Matchers.equalTo(0))
                .body("first", Matchers.equalTo(true))
                .body("last", Matchers.equalTo(true))
                .body("content.iata", Matchers.not(Matchers.hasItem("HEL")))
                .body("content.iata", Matchers.hasItem("JFK"));
    }

    @Test
    public void getAirportByIata() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .pathParams(ParameterNames.IATA,"xxx")
                .get(Path.AIRPORT_BY_IATA)
                .then()
                .assertThat()
                .statusCode(404)
                .body("message", Matchers.containsString("Resource not found for path variable 'iata' with value 'xxx'"));

        RestAssured.given()
                .spec(requestSpec)
                .when()
                .pathParams(ParameterNames.IATA,"itm")
                .get(Path.AIRPORT_BY_IATA)
                .then()
                .assertThat()
                .statusCode(200)
                .body("subdivision", Matchers.equalTo("Hyogo"));
    }
}
