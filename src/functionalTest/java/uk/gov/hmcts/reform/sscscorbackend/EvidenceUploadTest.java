package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;

@Ignore
public class EvidenceUploadTest extends BaseFunctionTest {
    @Test
    public void uploadThenDeleteEvidenceToQuestion() throws IOException, InterruptedException, JSONException {
        OnlineHearing hearingWithQuestion = createHearingWithQuestion(true);

        JSONObject questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        assertThat(questionResponse.has("evidence"), is(false));

        String fileName = "evidence.png";
        sscsCorBackendRequests.uploadEvidence(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId(), fileName);


        questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        JSONArray evidence = questionResponse.getJSONArray("evidence");
        assertThat(evidence.length(), is(1));
        assertThat(evidence.getJSONObject(0).getString("file_name"), is(fileName));

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

        String fileName = "evidence.png";
        sscsCorBackendRequests.uploadEvidence(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId(), fileName);


        questionResponse = sscsCorBackendRequests.getQuestion(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
        JSONArray evidence = questionResponse.getJSONArray("evidence");
        assertThat(evidence.length(), is(1));
        assertThat(evidence.getJSONObject(0).getString("file_name"), is(fileName));

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

        String fileName = "evidence.png";
        sscsCorBackendRequests.uploadHearingEvidence(hearingWithQuestion.getHearingId(), fileName);


        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(1));
        assertThat(draftHearingEvidence.getJSONObject(0).getString("file_name"), is(fileName));

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

        String fileName = "evidence.png";
        sscsCorBackendRequests.uploadHearingEvidence(hearingWithQuestion.getHearingId(), fileName);

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(1));
        assertThat(draftHearingEvidence.getJSONObject(0).getString("file_name"), is(fileName));

        sscsCorBackendRequests.submitHearingEvidence(hearingWithQuestion.getHearingId(), "some description");

        draftHearingEvidence = sscsCorBackendRequests.getDraftHearingEvidence(hearingWithQuestion.getHearingId());
        assertThat(draftHearingEvidence.length(), is(0));

        SscsCaseDetails caseDetails = getCaseDetails(hearingWithQuestion.getCaseId());

        List<SscsDocument> sscsDocument = caseDetails.getData().getSscsDocument();
        assertThat(sscsDocument.size(), is(2));
        String caseReference = caseDetails.getData().getCaseReference();
        assertThat(sscsDocument.get(0).getValue().getDocumentFileName(), is("Evidence Description - " + caseReference + ".pdf"));
        assertThat(sscsDocument.get(1).getValue().getDocumentFileName(), is(fileName));
    }

    @Test
    public void getEvidenceCoverSheet() throws IOException {
        OnlineHearing hearing = createHearing(true);

        String coversheet = sscsCorBackendRequests.getCoversheet(hearing.getHearingId());
        assertThat(coversheet, is("evidence_cover_sheet.pdf"));
    }
}
