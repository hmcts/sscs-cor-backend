package uk.gov.hmcts.reform.sscscorbackend.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class HealthTest {
    private final String baseUrl = System.getenv("TEST_URL");

    @Test
    public void testHealthEndpoint() {
        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
                .given()
                .when()
                .get("/health")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
