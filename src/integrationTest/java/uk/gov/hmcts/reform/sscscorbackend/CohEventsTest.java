package uk.gov.hmcts.reform.sscscorbackend;

import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohAnswers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohQuestionReference;

public class CohEventsTest extends BaseIntegrationTest {

    private Long caseId;
    private String hearingId;
    private byte[] pdf;
    private String caseReference;

    @Before
    public void createData() {
        caseId = 169L;
        hearingId = "someHearingId";
        pdf = new byte[]{1, 2, 3};
        caseReference = "caseRef" + ((int) (Math.random() * 1000));
    }

    @Test
    public void decisionIssuedCohEvent() throws IOException {
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        cohStub.stubGetDecisions(hearingId, caseId);
        cohStub.stubGetDecisionReplies(hearingId, "some_reply");
        documentStoreStub.stubUploadFile();
        pdfServiceStub.stubCreatePdf(pdf);
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        ccdStub.stubUpdateCase(caseId);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_DECISION_ISSUED.getCcdType());
        String cohEvent = createCohEvent("decision_issued");
        notificationsStub.stubSendNotification(cohEvent);

        makeCohEventRequest(cohEvent);

        pdfServiceStub.verifyCreateDecisionIssuedPdf(caseReference);
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Tribunals view - " + caseReference + ".pdf");
        mailStub.hasEmailWithSubjectAndAttachment("Preliminary view offered (" + caseReference + ")", pdf);
        notificationsStub.verifySendNotification(cohEvent);
    }

    @Test
    public void questionRoundIssuedCohEvent() throws IOException {
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        CohQuestionReference questionSummary = new CohQuestionReference(
                "first-id", 1, "first question", "first question body", "2018-01-01", someCohAnswers("answer_drafted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);

        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();

        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        ccdStub.stubUpdateCase(caseId);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_QUESTION_ROUND_ISSUED.getCcdType());
        String cohEvent = createCohEvent("question_round_issued");
        notificationsStub.stubSendNotification(cohEvent);

        makeCohEventRequest(cohEvent);

        notificationsStub.verifySendNotification(cohEvent);
        mailStub.hasEmailWithSubjectAndAttachment("Questions issued to the appellant (" + caseReference + ")", pdf);
        pdfServiceStub.verifyCreateQuestionRoundIssuedPdf(caseReference);
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Issued Questions Round 1 - " + caseReference + ".pdf");
    }

    @Test
    public void answerSubmittedCohEvent() throws IOException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        CohQuestionReference questionSummary = new CohQuestionReference(
                "first-id", 1, "first question", "first question body", "2018-01-01", someCohAnswers("answer_drafted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_ANSWERS_SUBMITTED.getCcdType());
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId);
        String cohEvent = createCohEvent("answers_submitted");

        makeCohEventRequest(cohEvent);

        mailStub.hasEmailWithSubjectAndAttachment("Appellant has provided information (" + caseReference + ")", pdf);
        pdfServiceStub.verifyCreateAnswersPdf(caseReference);
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Issued Answers Round 1 - " + caseReference + ".pdf");
    }

    @Test
    public void continiousOnlineHearingRelistedCohEvent() throws IOException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        cohStub.stubGetConversation(hearingId);
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId);
        String cohEvent = createCohEvent("continuous_online_hearing_relisted");
        notificationsStub.stubSendNotification(cohEvent);
        ccdStub.stubUpdateCaseWithEvent(caseId, "updateHearingType");
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_ONLINE_HEARING_RELISTED.getCcdType());

        makeCohEventRequest(cohEvent);

        mailStub.hasEmailWithSubject("COR: Hearing required");
        pdfServiceStub.verifySummaryPdf(caseReference);
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "COR Transcript - " + caseReference + ".pdf");
        ccdStub.verifyUpdateCaseToOralHearing(caseId, caseReference);
    }

    @Test
    public void questionDealineElapsedCohEvent() throws IOException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        CohQuestionReference questionSummary = new CohQuestionReference(
                "first-id", 1, "first question", "first question body", "2018-01-01", someCohAnswers("answer_drafted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_QUESTION_DEADLINE_ELAPSED.getCcdType());
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId);
        String cohEvent = createCohEvent("question_deadline_elapsed");
        notificationsStub.stubSendNotification(cohEvent);

        makeCohEventRequest(cohEvent);

        mailStub.hasEmailWithSubjectAndAttachment("Appellant has provided information (" + caseReference + ")", pdf);
        pdfServiceStub.verifyCreateAnswersPdf(caseReference);
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Issued Answers Deadline Elapsed Round 1 - " + caseReference + ".pdf");
        notificationsStub.verifySendNotification(cohEvent);
    }

    @Test
    public void questionDealineExtendedCohEvent() throws IOException {
        testEvent(EventType.COH_QUESTION_DEADLINE_EXTENDED, "question_deadline_extended", false);
    }

    @Test
    public void questionDealineExtensionDeniedCohEvent() throws IOException {
        testEvent(EventType.COH_QUESTION_DEADLINE_EXTENSION_DENIED, "question_deadline_extension_denied", false);
    }

    @Test
    public void questionDealineExtensionGrantedCohEvent() throws IOException {
        testEvent(EventType.COH_QUESTION_DEADLINE_EXTENSION_GRANTED, "question_deadline_extension_granted", false);
    }

    @Test
    public void questionDealineRemonderCohEvent() throws IOException {
        testEvent(EventType.COH_QUESTION_DEADLINE_REMINDER, "question_deadline_reminder", true);
    }

    @Test
    public void questionDealineRejectedCohEvent() throws IOException {
        testEvent(EventType.COH_DECISION_REJECTED, "decision_rejected", false);
    }

    private void testEvent(EventType eventType, String cohEventType, boolean sendNotification) throws JsonProcessingException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        ccdStub.stubUpdateCase(caseId);
        ccdStub.stubUpdateCaseWithEvent(caseId, eventType.getCcdType());
        String cohEvent = createCohEvent(cohEventType);
        if (sendNotification) {
            notificationsStub.stubSendNotification(cohEvent);
        }

        makeCohEventRequest(cohEvent);

        ccdStub.verifyUpdateCaseWithEvent(caseId, eventType.getCcdType());
        if (sendNotification) {
            notificationsStub.verifySendNotification(cohEvent);
        }
        mailStub.hasNoEmails();
    }

    private void makeCohEventRequest(String cohEvent) {
        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .contentType("application/json")
                .body(cohEvent)
                .post("/notify/onlinehearing")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private String createCohEvent(String eventType) {
        return "{\n" +
                "  \"case_id\": \"" + caseId + "\",\n" +
                "  \"event_type\": \"" + eventType + "\",\n" +
                "  \"online_hearing_id\": \"" + hearingId + "\"\n" +
                "}";
    }
}
