package uk.gov.hmcts.reform.sscscorbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class RecordTribunalViewResponseTest extends BaseIntegrationTest {

    private String hearingId;
    private Long caseId;

    @Before
    public void setUp() throws Exception {
        hearingId = "1";
        caseId = 123456L;
    }

    @Test
    public void recordAcceptedResponse() {
        String reply = "decision_accepted";
        String reason = "";
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        ccdStub.stubFindCaseByCaseId(caseId, "caseReference", "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        cohStub.stubPostDecisionReply(hearingId, reply, reason);
        getRequest()
                .body("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .patch("/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        mailStub.hasEmailWithSubject("caseworker@example.net", "Tribunal view accepted (caseReference)");
        mailStub.hasEmailWithSubject("dwp@example.com", "Tribunal view accepted (caseReference)");
    }

    @Test
    public void recordRejectedResponse() {
        String reply = "decision_rejected";
        String reason = "Reasons for rejecting tribunal's view";
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        ccdStub.stubFindCaseByCaseId(caseId, "caseReference", "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        cohStub.stubPostDecisionReply(hearingId, reply, reason);
        getRequest()
                .body("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .patch("/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        mailStub.hasEmailWithSubject("caseworker@example.net", "Tribunal view rejected (caseReference)");
    }

    @Test
    public void recordRejectedResponseWithoutReason() {
        String reply = "decision_rejected";
        String reason = "";
        getRequest()
                .body("{\"reply\":\"" + reply + "\", \"reason\":\"" + reason + "\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .patch("/continuous-online-hearings/" + hearingId + "/tribunal-view")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
