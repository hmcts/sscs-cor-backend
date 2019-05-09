package uk.gov.hmcts.reform.sscscorbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class EvidenceTest extends BaseIntegrationTest {
    private final Long caseId = 123L;
    private final String hearingId = "hearingId";
    private String caseReference = "caseReference";

    @Test
    public void sendsEvidenceToDwp() throws JsonProcessingException {
        cohStub.stubGetOnlineHearing(caseId, hearingId);

        ccdStub.stubFindCaseByCaseId(caseId, caseReference, "first-id", "someEvidence", "evidenceCreatedDate", "http://example.com/documents/1");
        byte[] pdf = {2, 4, 6, 0, 1};
        pdfServiceStub.stubCreatePdf(pdf);
        documentStoreStub.stubUploadFile();
        ccdStub.stubUpdateCase(caseId, caseReference);
        ccdStub.stubUpdateCaseWithEvent(caseId, "uploadCorDocument", caseReference);
        String evidenceFileContent = "evidence file content";
        documentStoreStub.stubGetFile("/documents/123", evidenceFileContent);

        getRequest()
                .body("{\"body\":\"some evidence description\"}")
                .when()
                .contentType(APPLICATION_JSON_VALUE)
                .post("/continuous-online-hearings/" + hearingId + "/evidence")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        mailStub.hasEmailWithSubjectAndAttachment("Evidence uploaded (caseReference)", pdf);
        mailStub.hasEmailWithSubjectAndAttachment("Evidence uploaded (caseReference)", evidenceFileContent.getBytes());
    }
}
