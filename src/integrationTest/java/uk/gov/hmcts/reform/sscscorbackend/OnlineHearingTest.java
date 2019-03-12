package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;

public class OnlineHearingTest extends BaseIntegrationTest {

    private final String email = "foo@bar.com";
    private final String expectedOnlineHearingId = "someOnlineHearingId";
    private final Long caseId = 1234321L;
    private final String expectedCaseReference = "someCaseReference";
    private final String firstName = "firstName";
    private final String lastName = "lastName";

    @Test
    public void getsOnlineHearing() throws UnsupportedEncodingException, JsonProcessingException {
        ccdStub.stubSearchCaseWithEmailAddress(email, caseId, expectedCaseReference, firstName, lastName);
        ccdStub.stubGetHistoryEvents(caseId, EventType.FINAL_DECISION);
        cohStub.stubGetOnlineHearing(caseId, expectedOnlineHearingId);
        cohStub.stubGetDecisionNotFound(expectedOnlineHearingId);
        cohStub.stubGetDecisionRepliesNotFound(expectedOnlineHearingId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings?email=" + email)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("online_hearing_id", equalTo(expectedOnlineHearingId))
                .body("case_reference", equalTo(expectedCaseReference))
                .body("appellant_name", equalTo(firstName + " " + lastName))
                .body("decision", nullValue());
    }

    @Test
    public void getsOnlineHearingWithDecision() throws UnsupportedEncodingException, JsonProcessingException {
        ccdStub.stubSearchCaseWithEmailAddress(email, caseId, expectedCaseReference, firstName, lastName);
        ccdStub.stubGetHistoryEvents(caseId, EventType.FINAL_DECISION);
        cohStub.stubGetOnlineHearing(caseId, expectedOnlineHearingId);
        cohStub.stubGetDecisions(expectedOnlineHearingId, caseId);
        cohStub.stubGetDecisionRepliesEmpty(expectedOnlineHearingId);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings?email=" + email)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("online_hearing_id", equalTo(expectedOnlineHearingId))
                .body("case_reference", equalTo(expectedCaseReference))
                .body("appellant_name", equalTo(firstName + " " + lastName))
                .body("decision.decision_state", equalTo("decision_issued"))
                .body("decision.decision_state_datetime", equalTo("2018-10-05T09:36:33Z"));
    }
}
