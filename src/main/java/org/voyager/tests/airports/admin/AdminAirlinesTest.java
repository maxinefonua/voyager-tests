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
import org.voyager.tests.config.FunctionalTestConfig;

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

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"))
                .body("message", Matchers.containsString("airline"))
                .body("message", Matchers.containsString("isActive"))
                .body("message", Matchers.containsString("iataList"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body("{\"airline\":\"delta\",\"isActive\":true,\"iataList\":[]}")
                .when()
                .post(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("iataList"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body("{\"airline\":\"delta\",\"isActive\":true,\"iataList\":[\"SJC\",\"SFO\"]}")
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
                .queryParam(ParameterNames.AIRLINE_PARAM_NAME,"")
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Missing required request parameter 'airline'"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .queryParam(ParameterNames.AIRLINE_PARAM_NAME,"fakeairline")
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'airline' with value 'fakeairline'"));

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .queryParam(ParameterNames.AIRLINE_PARAM_NAME,"zipair")
                .delete(Path.Admin.AIRLINES)
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.notNullValue());
    }
}
