package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class EvidenceUploadTest extends BaseFunctionTest {

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
        assertThat(scannedDocument.size(), is(1));
        String expectedEvidenceUploadFilename = String.format("Appellant upload 1 - %s.pdf", caseDetails.getId());
        assertThat(scannedDocument.get(0).getValue().getFileName(), is(expectedEvidenceUploadFilename));
    }

    @Test
    public void getEvidenceCoverSheet() throws IOException {
        CreatedCcdCase createdCcdCase = createCase();

        String coversheet = sscsCorBackendRequests.getCoversheet(createdCcdCase.getCaseId());
        assertThat(coversheet, is("evidence_cover_sheet.pdf"));
    }
}
