package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;
import org.junit.Test;

public class AppellantStatementTest extends BaseFunctionTest {
    @Test
    public void canUploadAnAppellantStatement() throws IOException {
        OnlineHearing hearingWithQuestion = createHearing(true);

        sscsCorBackendRequests.uploadAppellantStatement(hearingWithQuestion.getCaseId(), "statement");
    }
}
