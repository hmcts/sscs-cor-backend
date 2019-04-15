package uk.gov.hmcts.reform.sscscorbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class AppellantStatementTest extends BaseIntegrationTest {
    private final Long caseId = 123L;
    private final String hearingId = "hearingId";
    private String caseReference = "caseReference";
    
    @Test
    public void recordRejectedResponse() throws JsonProcessingException {
        cohStub.stubGetOnlineHearing(caseId, hearingId);

        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/document/1");
        documentStoreStub.stubUploadFile();
        byte[] pdf = {2, 4, 6, 0, 1};
        pdfServiceStub.stubCreatePdf(pdf);
        ccdStub.stubUpdateCase(caseId, caseReference);

        getRequest()
                .body("{\"body\":\"some appellant statement\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .post("/continuous-online-hearings/" + hearingId + "/statement")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        mailStub.hasEmailWithSubjectAndAttachment("COR: Additional evidence submitted (caseReference)", pdf);
    }
}
