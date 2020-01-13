package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;

public class EvidenceUploadTest extends BaseFunctionTest {
    @Test
    public void uploadThenDeleteEvidenceToQuestion() throws IOException, InterruptedException, JSONException {
        OnlineHearing hearingWithQuestion = createHearingWithQuestion(true);

        JSONObject questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        assertThat(questionResponse.has("evidence"), is(false));

        sscsCorBackendRequests.uploadEvidence(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId(), "evidence.png");

        questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        JSONArray evidence = questionResponse.getJSONArray("evidence");
        assertThat(evidence.length(), is(1));
        assertThat(evidence.getJSONObject(0).getString("file_name"), is("evidence.pdf"));

        String evidenceId = evidence.getJSONObject(0).getString("id");

        sscsCorBackendRequests.deleteEvidence(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId(), evidenceId);
        questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        assertThat(questionResponse.has("evidence"), is(false));
    }

    @Test
    public void uploadEvidenceThenSubmitQuestion() throws IOException, InterruptedException, JSONException {
        OnlineHearing hearingWithQuestion = createHearingWithQuestion(true);

        JSONObject questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        assertThat(questionResponse.has("evidence"), is(false));

        sscsCorBackendRequests.uploadEvidence(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId(), "evidence.png");

        questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        JSONArray evidence = questionResponse.getJSONArray("evidence");
        assertThat(evidence.length(), is(1));
        assertThat(evidence.getJSONObject(0).getString("file_name"), is("evidence.pdf"));

        sscsCorBackendRequests.answerQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId(), "some answer");
        sscsCorBackendRequests.submitAnswer(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        assertThat(questionResponse.has("evidence"), is(true));
    }

    @Test
    public void uploadThenDeleteEvidenceToHearing() throws IOException, JSONException {
        OnlineHearing hearingWithQuestion = createHearing(true);

        JSONArray draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(0));

        sscsCorBackendRequests.uploadHearingEvidence(hearingWithQuestion.getHearingId(), "evidence.png");

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(1));
        assertThat(draftHearingEvidence.getJSONObject(0).getString("file_name"), is("evidence.pdf"));

        String evidenceId = draftHearingEvidence.getJSONObject(0).getString("id");

        sscsCorBackendRequests.deleteHearingEvidence(hearingWithQuestion.getHearingId(), evidenceId);
        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(0));
    }

    @Test
    public void uploadThenSubmitEvidenceToHearing() throws IOException, JSONException {
        OnlineHearing hearingWithQuestion = createHearing(true);

        JSONArray draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(0));

        sscsCorBackendRequests.uploadHearingEvidence(hearingWithQuestion.getHearingId(), "evidence.png");

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(1));
        assertThat(draftHearingEvidence.getJSONObject(0).getString("file_name"), is("evidence.pdf"));

        sscsCorBackendRequests.submitHearingEvidence(hearingWithQuestion.getHearingId(), "some description");

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(0));

        SscsCaseDetails caseDetails = getCaseDetails(hearingWithQuestion.getCaseId());

        List<SscsDocument> sscsDocument = caseDetails.getData().getSscsDocument();
        assertThat(sscsDocument.size(), is(2));
        String caseReference = caseDetails.getData().getCaseReference();
        assertThat(sscsDocument.get(0).getValue().getDocumentFileName(), is("evidence.pdf"));
        assertThat(sscsDocument.get(1).getValue().getDocumentFileName(), is("Evidence Description - " + hearingWithQuestion.getCaseId() + ".pdf"));
    }

    @Test
    public void uploadThenSubmitEvidenceToAppeal() throws IOException, JSONException {
        CreatedCcdCase createdCcdCase = createCase();

        JSONArray draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(createdCcdCase.getCaseId());
        assertThat(draftHearingEvidence.length(), is(0));

        sscsCorBackendRequests.uploadHearingEvidence(createdCcdCase.getCaseId(), "evidence.png");

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(createdCcdCase.getCaseId());
        assertThat(draftHearingEvidence.length(), is(1));
        assertThat(draftHearingEvidence.getJSONObject(0).getString("file_name"), is("evidence.pdf"));

        sscsCorBackendRequests.submitHearingEvidence(createdCcdCase.getCaseId(), "some description");

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(createdCcdCase.getCaseId());
        assertThat(draftHearingEvidence.length(), is(0));

        SscsCaseDetails caseDetails = getCaseDetails(createdCcdCase.getCaseId());

        List<ScannedDocument> scannedDocument = caseDetails.getData().getScannedDocuments();
        assertThat(scannedDocument.size(), is(2));
        String caseReference = caseDetails.getData().getCaseReference();
        assertThat(scannedDocument.get(0).getValue().getFileName(), is("evidence.pdf"));
        assertThat(scannedDocument.get(1).getValue().getFileName(), is("Evidence Description - " + createdCcdCase.getCaseId() + ".pdf"));
    }


    @Test
    public void getEvidenceCoverSheet() throws IOException {
        OnlineHearing hearing = createHearing(true);

        String coversheet = sscsCorBackendRequests.getCoversheet(hearing.getHearingId());
        assertThat(coversheet, is("evidence_cover_sheet.pdf"));
    }
}
