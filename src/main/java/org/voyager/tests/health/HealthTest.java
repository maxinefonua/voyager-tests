package org.voyager.tests.health;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.tests.config.FunctionalTestConfig;
import org.voyager.tests.config.HealthConfig;

public class HealthTest {
    private static RequestSpecification requestSpec;
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = FunctionalTestConfig.getBaseUri();
        String authToken = FunctionalTestConfig.getUserAuthToken();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(Headers.AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testAPIHealth() {
        RestAssured.given()
                    .spec(requestSpec)
                    .when()
                    .get(HealthConfig.getHealthPath())
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body("status", Matchers.equalTo("UP"));
    }
}
