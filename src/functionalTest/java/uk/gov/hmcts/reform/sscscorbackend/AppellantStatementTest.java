package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;
import org.junit.Test;

public class AppellantStatementTest extends BaseFunctionTest {
    @Test
    public void canUploadAnAppellantStatement() throws IOException {
        OnlineHearing hearingWithQuestion = createHearing(true);

        sscsCorBackendRequests.uploadAppellantStatement(hearingWithQuestion.getHearingId(), "statement");
    }

    @Test
    public void canUploadAnAppellantStatementForDigital() throws IOException {
        CreatedCcdCase createdCase = createCase();

        sscsCorBackendRequests.uploadAppellantStatement(createdCase.getCaseId(), "statement");
    }
}
