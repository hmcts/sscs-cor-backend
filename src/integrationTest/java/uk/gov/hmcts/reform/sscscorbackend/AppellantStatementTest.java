package uk.gov.hmcts.reform.sscscorbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class AppellantStatementTest extends BaseIntegrationTest {
    private final Long caseId = 123L;
    private String caseReference = "caseReference";

    @Test
    public void recordAppellantStatement() throws JsonProcessingException {
        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence.pdf", "evidenceCreatedDate", "http://example.com/document/1");
        documentStoreStub.stubUploadFile();
        byte[] pdf = {2, 4, 6, 0, 1};
        pdfServiceStub.stubCreatePdf(pdf);
        ccdStub.stubUpdateCase(caseId, caseReference);

        getRequest()
                .body("{\"body\":\"some appellant statement\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .post("/continuous-online-hearings/" + caseId + "/statement")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        ccdStub.verifyUpdateCaseWithPdfToScannedDocuments(caseId, caseReference, "Appellant statement 1 - 123.pdf");
    }
}
