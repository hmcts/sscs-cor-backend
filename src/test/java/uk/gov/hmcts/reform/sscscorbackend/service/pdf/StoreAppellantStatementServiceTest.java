package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.OldPdfService;

@RunWith(JUnitParamsRunner.class)
public class StoreAppellantStatementServiceTest {

    private StoreAppellantStatementService storeAppellantStatementService;

    @Before
    public void setUp() {
        storeAppellantStatementService = new StoreAppellantStatementService(
            mock(OldPdfService.class),
            "templatePath",
            mock(CcdPdfService.class),
            mock(IdamService.class),
            mock(EvidenceManagementService.class)
        );
    }

    @Test
    @Parameters(method = "generateDifferentCaseDataScenarios")
    public void getDocumentPrefixWhenNoOtherDocuments(SscsCaseData sscsCaseData, String expectedFileName) {
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(sscsCaseData).build();
        String documentPrefix = storeAppellantStatementService.documentNamePrefix(sscsCaseDetails,
            "onlineHearingId");

        assertThat(documentPrefix, is(expectedFileName));
    }

    private Object[] generateDifferentCaseDataScenarios() {
        SscsCaseData sscsCaseDataWithNoDocs = SscsCaseData.builder().build();
        SscsCaseData sscsCaseDataWithSomeOtherDoc = caseWithDocument("Some other document.txt");
        SscsCaseData sscsCaseDataWithSomeOtherStatement = caseWithDocument(
            "Appellant statement 1 - SC0011111.pdf");
        SscsCaseData sscsCaseDataWithDocWithNullValue = SscsCaseData.builder()
            .scannedDocuments(singletonList(ScannedDocument.builder()
                .value(null)
                .build()))
            .build();
        SscsCaseData sscsCaseDataWithDocWithEmptyFilename = SscsCaseData.builder()
            .scannedDocuments(singletonList(ScannedDocument.builder()
                .value(ScannedDocumentDetails.builder()
                    .fileName("")
                    .build())
                .build()))
            .build();
        SscsCaseData sscsCaseDataWithDocWithNullFilename = SscsCaseData.builder()
            .scannedDocuments(singletonList(ScannedDocument.builder()
                .value(ScannedDocumentDetails.builder()
                    .fileName(null)
                    .build())
                .build()))
            .build();
        return new Object[]{
            new Object[]{sscsCaseDataWithNoDocs, "Appellant statement 1 - "},
            new Object[]{sscsCaseDataWithSomeOtherDoc, "Appellant statement 1 - "},
            new Object[]{sscsCaseDataWithSomeOtherStatement, "Appellant statement 2 - "},
            new Object[]{sscsCaseDataWithDocWithNullValue, "Appellant statement 1 - "},
            new Object[]{sscsCaseDataWithDocWithEmptyFilename, "Appellant statement 1 - "},
            new Object[]{sscsCaseDataWithDocWithNullFilename, "Appellant statement 1 - "}
        };
    }

    private SscsCaseData caseWithDocument(String documentFileName) {
        return SscsCaseData.builder()
            .scannedDocuments(singletonList(ScannedDocument.builder()
                .value(ScannedDocumentDetails.builder()
                    .fileName(documentFileName)
                    .build())
                .build()))
            .build();
    }
}