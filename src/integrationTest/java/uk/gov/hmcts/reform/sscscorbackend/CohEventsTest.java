package uk.gov.hmcts.reform.sscscorbackend;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohAnswers;
import static uk.gov.hmcts.reform.sscscorbackend.stubs.CcdStub.baseCaseData;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
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
        ccdStub.stubUpdateCase(caseId, caseReference);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_DECISION_ISSUED.getCcdType(), caseReference);
        ccdStub.stubGetHistoryEvents(caseId, EventType.SYA_APPEAL_CREATED);
        String cohEvent = createCohEvent("decision_issued");
        notificationsStub.stubSendNotification(cohEvent);

        makeCohEventRequest(cohEvent);

        pdfServiceStub.verifyCreateDecisionIssuedPdf(caseReference);
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Tribunals view - " + caseId + ".pdf");
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
        ccdStub.stubUpdateCase(caseId, caseReference);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_QUESTION_ROUND_ISSUED.getCcdType(), caseReference);
        String cohEvent = createCohEvent("question_round_issued");
        notificationsStub.stubSendNotification(cohEvent);

        makeCohEventRequest(cohEvent);

        notificationsStub.verifySendNotification(cohEvent);
        mailStub.hasEmailWithSubjectAndAttachment("Questions issued to the appellant (" + caseReference + ")", pdf);
        pdfServiceStub.verifyCreateQuestionRoundIssuedPdf(caseId.toString());
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Issued Questions Round 1 - " + caseId + ".pdf");
    }

    @Test
    public void answerSubmittedCohEvent() throws IOException {
        String evidenceUrl = "http://localhost:4603/documents/123";
        String questionId = "first-id";
        SscsCaseData.SscsCaseDataBuilder sscsCaseData = createSscsCaseDataWithQuestionEvidence(evidenceUrl, questionId);
        ccdStub.stubFindCaseByCaseId(caseId, sscsCaseData);


        CohQuestionReference questionSummary = new CohQuestionReference(
                questionId, 1, "first question", "first question body", "2018-01-01", someCohAnswers("answer_submitted")
        );
        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_ANSWERS_SUBMITTED.getCcdType(), caseReference);
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId, caseReference);
        String cohEvent = createCohEvent("answers_submitted");

        String evidenceFileContent = "evidence file content";
        documentStoreStub.stubGetFile("/documents/123", evidenceFileContent);

        makeCohEventRequest(cohEvent);

        mailStub.hasEmailWithSubjectAndAttachment("Appellant has provided information (" + caseReference + ")", pdf);
        pdfServiceStub.verifyCreateAnswersPdf(caseId.toString());
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Issued Answers Round 1 - " + caseId + ".pdf");

        mailStub.hasEmailWithSubjectAndAttachment("Evidence uploaded (" + caseReference + ")", evidenceFileContent.getBytes());
    }

    @Test
    public void continiousOnlineHearingRelistedCohEvent() throws IOException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        cohStub.stubGetConversation(hearingId);
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId, caseReference);
        String cohEvent = createCohEvent("continuous_online_hearing_relisted");
        notificationsStub.stubSendNotification(cohEvent);
        ccdStub.stubUpdateCaseWithEvent(caseId, "updateHearingType", caseReference);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_ONLINE_HEARING_RELISTED.getCcdType(), caseReference);
        ccdStub.stubRemovePanelMember(caseId, "someMedicalMember");
        ccdStub.stubRemovePanelMember(caseId, "someDisabilityMember");

        makeCohEventRequest(cohEvent);

        mailStub.hasEmailWithSubject("COR: Hearing required");
        pdfServiceStub.verifySummaryPdf(caseId.toString());
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "COR Transcript - " + caseId + ".pdf");
        ccdStub.verifyUpdateCaseToOralHearing(caseId, caseReference);
        ccdStub.verifyRemovePanelMember(caseId, "someMedicalMember");
        ccdStub.verifyRemovePanelMember(caseId, "someDisabilityMember");
    }

    @Test
    public void questionDealineElapsedCohEvent() throws IOException {
        String evidenceUrl = "http://localhost:4603/documents/123";
        String questionId = "first-id";
        SscsCaseData.SscsCaseDataBuilder sscsCaseData = createSscsCaseDataWithQuestionEvidence(evidenceUrl, questionId);
        ccdStub.stubFindCaseByCaseId(caseId, sscsCaseData);
        CohQuestionReference questionSummary = new CohQuestionReference(
                questionId, 1, "first question", "first question body", "2018-01-01", someCohAnswers("answer_submitted")
        );
        String evidenceFileContent = "evidence file content";
        documentStoreStub.stubGetFile("/documents/123", evidenceFileContent);

        cohStub.stubGetAllQuestionRounds(hearingId, questionSummary);
        cohStub.stubGetOnlineHearing(caseId, hearingId);
        ccdStub.stubUpdateCaseWithEvent(caseId, EventType.COH_QUESTION_DEADLINE_ELAPSED.getCcdType(), caseReference);
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId, caseReference);
        String cohEvent = createCohEvent("question_deadline_elapsed");
        notificationsStub.stubSendNotification(cohEvent);

        makeCohEventRequest(cohEvent);

        mailStub.hasEmailWithSubjectAndAttachment("Appellant has provided information (" + caseReference + ")", pdf);
        mailStub.hasEmailWithSubjectAndAttachment("Evidence uploaded (" + caseReference + ")", evidenceFileContent.getBytes());
        pdfServiceStub.verifyCreateAnswersPdf(caseId.toString());
        documentStoreStub.verifyUploadFile(pdf);
        ccdStub.verifyUpdateCaseWithPdf(caseId, caseReference, "Issued Answers Deadline Elapsed Round 1 - " + caseId + ".pdf");
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

    private SscsCaseData.SscsCaseDataBuilder createSscsCaseDataWithQuestionEvidence(String evidenceUrl, String questionId) {
        return baseCaseData(caseReference)
                .corDocument(singletonList(CorDocument.builder()
                        .value(CorDocumentDetails.builder()
                                .questionId(questionId)
                                .document(SscsDocumentDetails.builder()
                                        .documentFileName("someEvidence")
                                        .documentLink(DocumentLink.builder()
                                                .documentUrl(evidenceUrl)
                                                .documentBinaryUrl(evidenceUrl)
                                                .build())
                                        .documentDateAdded("evidenceCreatedDate")
                                        .build())
                                .build())
                        .build()));
    }

    private void testEvent(EventType eventType, String cohEventType, boolean sendNotification) throws JsonProcessingException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        ccdStub.stubUpdateCase(caseId, caseReference);
        ccdStub.stubUpdateCaseWithEvent(caseId, eventType.getCcdType(), caseReference);
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
