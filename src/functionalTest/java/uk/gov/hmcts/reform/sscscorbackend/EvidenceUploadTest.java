package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;
import org.junit.Test;

public class EvidenceUploadTest extends BaseFunctionTest {
    @Test
    public void uploadEvidence() throws IOException, InterruptedException {
        OnlineHearing hearingWithQuestion = createHearingWithQuestion(true);

        sscsCorBackendRequests.uploadEvidence(hearingWithQuestion.getHearingId(), hearingWithQuestion.getQuestionId());
    }
}
