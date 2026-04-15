package org.voyager.tests.airports.admin;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.tests.config.FunctionalTestConfig;
import java.util.List;

public class AdminRouteSyncTest {
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
    public void authenticateAddRoute() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .post(Path.Admin.ROUTES)
                .then()
                .assertThat()
                .statusCode(403);

        RouteForm routeForm = RouteForm.builder()
                .origin("AAA")
                .destination("ZZZ")
                .build();

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .body(routeForm)
                .when()
                .post(Path.Admin.ROUTES)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request body"));
    }

    @Test
    public void authenticateGetRouteSyncById() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .pathParams(ParameterNames.ID,1)
                .get(Path.Admin.ROUTES.concat(Path.Admin.SYNC_BY_ID))
                .then()
                .assertThat()
                .statusCode(403);

        RestAssured.given()
                .spec(adminRequestSpec)
                .contentType(ContentType.JSON)
                .when()
                .pathParams(ParameterNames.ID,1)
                .get(Path.Admin.ROUTES.concat(Path.Admin.SYNC_BY_ID))
                .then()
                .assertThat()
                .statusCode(200);
    }

    @Test
    public void getRouteSyncByRouteId() {
        List<Route> routeList = RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.ORIGIN,"SJC")
                .queryParam(ParameterNames.DESTINATION,"HNL")
                .get(Path.ROUTES)
                .getBody().as(new TypeRef<List<Route>>() {});
        assert routeList != null;
        assert !routeList.isEmpty();
        Route route = routeList.get(0);
        RestAssured.given()
                .spec(adminRequestSpec)
                .when()
                .pathParams(ParameterNames.ID,route.getId())
                .get(Path.Admin.ROUTES.concat(Path.Admin.SYNC_BY_ID))
                .then()
                .assertThat()
                .statusCode(200)
                .body("", Matchers.not(Matchers.empty()))
                .body("id",Matchers.equalTo(route.getId()));
    }
}
