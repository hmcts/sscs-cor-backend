package uk.gov.hmcts.reform.sscscorbackend;

import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohAnswers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohQuestionReference;

public class OnlineHearingControllerTest extends BaseIntegrationTest {
    private static final String ONLINE_HEARING_ID = "Online hearing id";

    @Test
    public void createOnlineHearing() throws JsonProcessingException {
        cohStub.stubPostOnlineHearing(ONLINE_HEARING_ID);
        ccdStub.stubAddUserToCase(123456, "medical");
        ccdStub.stubAddUserToCase(123456, "disability");

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(postOnlineHearingJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("onlineHearingCreated", Matchers.is(true));
    }

    @Test
    public void get400WhenNoPanel() {
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(postOnlineHearingNoPanelJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void postCohEvent() throws JsonProcessingException {
        Long caseId = 169L;
        String hearingId = "someHearingId";
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        cohStub.stubGetDecisions(hearingId, caseId);
        cohStub.stubGetDecisionReplies(hearingId, "some_reply");
        documentStoreStub.stubUploadFile();
        pdfServiceStub.stubCreatePdf(new byte[]{1, 2, 3});
        ccdStub.stubFindCaseByCaseId(caseId, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        ccdStub.stubUpdateCase(caseId);
        String cohEvent = "{\n" +
                "  \"case_id\": \"" + caseId + "\",\n" +
                "  \"event_type\": \"decision_issued\",\n" +
                "  \"online_hearing_id\": \"" + hearingId + "\"\n" +
                "}";
        notificationsStub.stubSendNotification(cohEvent);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(cohEvent)
                .post("/notify/onlinehearing")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void sendDwpEmail() throws JsonProcessingException {
        Long caseId = 169L;
        String hearingId = "someHearingId";
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        CohQuestionReference questionSummary = new CohQuestionReference(
                "first-id", 1, "first question", "first question body", "2018-01-01", someCohAnswers("answer_drafted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);

        byte[] questionsPdf = {1, 2, 3};
        pdfServiceStub.stubCreatePdf(questionsPdf);
        documentStoreStub.stubUploadFile();
        String caseRef = "caseRef" + ((int) (Math.random() * 1000));

        ccdStub.stubFindCaseByCaseId(caseId, caseRef, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        ccdStub.stubUpdateCase(caseId);
        String cohEvent = "{\n" +
                "  \"case_id\": \"" + caseId + "\",\n" +
                "  \"event_type\": \"question_round_issued\",\n" +
                "  \"online_hearing_id\": \"" + hearingId + "\"\n" +
                "}";
        notificationsStub.stubSendNotification(cohEvent);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(cohEvent)
                .post("/notify/onlinehearing")
                .then()
                .statusCode(HttpStatus.OK.value());

        mailStub.hasEmailWithSubjectAndAttachment("Questions issued to the appellant (" + caseRef + ")", questionsPdf);
    }

    private static final String postOnlineHearingJson = "{\n" +
            "  \"case_details\": {\n" +
            "    \"id\": \"123456\",\n" +
            "    \"case_data\": {\n" +
            "        \"onlineHearingId\": \"string\",\n" +
            "        \"hearingType\": \"cor\",\n" +
            "        \"assignedToJudge\": \"judge\",\n" +
            "        \"assignedToDisabilityMember\": \"disability\",\n" +
            "        \"assignedToMedicalMember\": \"medical\"\n" +
            "      }\n" +
            "  }\n" +
            "}";
    private static final String postOnlineHearingNoPanelJson = "{\n" +
            "  \"case_details\": {\n" +
            "    \"id\": \"string\",\n" +
            "    \"case_data\": {\n" +
            "    \"onlineHearingId\": \"string\",\n" +
            "  }\n" +
            "  }\n" +
            "}";
}
