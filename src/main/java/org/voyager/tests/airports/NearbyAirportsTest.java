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
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.tests.config.FunctionalTestConfig;

public class NearbyAirportsTest {
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
    public void nearbyAirportsIata() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Missing required request parameter 'iata'"));

        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ParameterNames.IATA,"SLC")
                .when()
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(5))
                .body("[0].iata", Matchers.equalTo("SLC"))
                .body("[1].iata", Matchers.equalTo("BTF"))
                .body("[2].iata", Matchers.equalTo("HIF"));

        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ParameterNames.IATA,"SLC")
                .queryParam(ParameterNames.TYPE,"military")
                .when()
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(5))
                .body("[0].iata", Matchers.equalTo("HIF"))
                .body("[1].iata", Matchers.equalTo("MUO"))
                .body("[2].iata", Matchers.equalTo("XSD"));

        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ParameterNames.IATA,"SLC")
                .queryParam(ParameterNames.AIRLINE,"delta")
                .when()
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(5))
                .body("[0].iata", Matchers.equalTo("SLC"))
                .body("[1].iata", Matchers.equalTo("PIH"))
                .body("[2].iata", Matchers.equalTo("TWF"));

        RestAssured.given()
                .spec(requestSpec)
                .queryParam(ParameterNames.IATA,"SLC")
                .queryParam(ParameterNames.AIRLINE,"delta,united,american")
                .when()
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(5))
                .body("[0].iata", Matchers.equalTo("SLC"))
                .body("[1].iata", Matchers.equalTo("PVU"))
                .body("[2].iata", Matchers.equalTo("PIH"));
    }

    @Test
    public void nearbyAirportsCoordinates() {
        Double latitudeOfSLC = 40.7695;
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .queryParam(ParameterNames.LATITUDE,latitudeOfSLC)
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", Matchers.containsString("Invalid request parameter 'longitude'"));


        Double longitudeOfSLC = -111.8912;
        RestAssured.given()
                .spec(requestSpec)
                .queryParams(ParameterNames.LATITUDE,latitudeOfSLC,
                        ParameterNames.LONGITUDE,longitudeOfSLC,
                        ParameterNames.LIMIT,3,
                        ParameterNames.TYPE, AirportType.CIVIL.name(),
                        ParameterNames.AIRLINE,Airline.SOUTHWEST.name())
                .when()
                .get(Path.NEARBY_AIRPORTS)
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", Matchers.equalTo(3))
                .body("[0].iata", Matchers.equalTo("SLC"))
                .body("[1].iata", Matchers.equalTo("MTJ"))
                .body("[2].iata", Matchers.equalTo("BOI"));
    }
}
