package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.equalTo;

import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class OnlineHearingTest extends BaseIntegrationTest {
    @Test
    public void getsOnlineHearing() throws UnsupportedEncodingException {
        String email = "foo@bar.com";
        String expectedOnlineHearingId = "someOnlineHearingId";
        cohStub.stubGetOnlineHearing(email, expectedOnlineHearingId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings?email=" + email)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("online_hearing_id", equalTo(expectedOnlineHearingId));
    }
}
