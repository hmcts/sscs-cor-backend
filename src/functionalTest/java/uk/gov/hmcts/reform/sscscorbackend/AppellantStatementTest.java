package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;
import org.junit.Test;

public class AppellantStatementTest extends BaseFunctionTest {

    @Test
    public void canUploadAnAppellantStatementForDigital() throws IOException {
        CreatedCcdCase createdCase = createCase();

        sscsCorBackendRequests.uploadAppellantStatement(createdCase.getCaseId(), "statement");
    }
}
