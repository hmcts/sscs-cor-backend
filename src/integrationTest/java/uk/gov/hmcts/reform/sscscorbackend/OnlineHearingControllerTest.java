package uk.gov.hmcts.reform.sscscorbackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class OnlineHearingControllerTest extends BaseIntegrationTest {
    private static final String ONLINE_HEARING_ID = "Online hearing id";

    @Test
    public void createOnlineHearing() {
        cohStub.stubPostOnlineHearing(ONLINE_HEARING_ID);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(postOnlineHearingJson)
                .when()
                .post("/notify/onlineappeal")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("online_hearing_id", Matchers.is(ONLINE_HEARING_ID));
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
        pdfServiceStub.stubCreatePdf();
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

    private static final String postOnlineHearingJson = "{\n" +
            "  \"case_details\": {\n" +
            "    \"id\": \"string\",\n" +
            "    \"case_data\": {\n" +
            "        \"onlineHearingId\": \"string\",\n" +
            "        \"hearingType\": \"cor\",\n" +
            "        \"onlinePanel\": {\n" +
            "            \"assignedTo\": \"string\",\n" +
            "            \"medicalMember\": \"string\",\n" +
            "            \"disabilityQualifiedMember\": \"string\"\n" +
            "        }\n" +
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
