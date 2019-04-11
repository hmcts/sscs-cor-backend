package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

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
    public void uploadThenDeleteEvidenceToHearing() throws IOException, InterruptedException, JSONException {
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
    public void getEvidenceCoverSheet() throws IOException {
        OnlineHearing hearing = createHearing(true);

        String coversheet = sscsCorBackendRequests.getCoversheet(hearing.getHearingId());
        assertThat(coversheet, is("evidence_cover_sheet.pdf"));
    }
}
