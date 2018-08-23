package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class OnlineHearingTest extends BaseIntegrationTest {
    @Test
    public void getsOnlineHearing() throws UnsupportedEncodingException, JsonProcessingException {
        String email = "foo@bar.com";
        String expectedOnlineHearingId = "someOnlineHearingId";
        Long caseId = 1234321L;
        String expectedCaseReference = "someCaseReference";
        String firstName = "firstName";
        String lastName = "lastName";
        ccdStub.stubSearchCaseWithEmailAddress(email, caseId, expectedCaseReference, firstName, lastName);
        cohStub.stubGetOnlineHearing(caseId, expectedOnlineHearingId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings?email=" + email)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("online_hearing_id", equalTo(expectedOnlineHearingId))
                .body("case_reference", equalTo(expectedCaseReference))
                .body("appellant_name", equalTo(firstName + " " + lastName));
    }
}
