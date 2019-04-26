package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocumentDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.OldPdfService;

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
    public void getDocumentPrefixWhenNoOtherDocuments() {
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(SscsCaseData.builder().build()).build();
        String documentPrefix = storeAppellantStatementService.documentNamePrefix(sscsCaseDetails, "onlineHearing");

        assertThat(documentPrefix, is("Appellant statement 1 - "));
    }

    @Test
    public void getDocumentPrefixWhenOtherDocuments() {
        SscsCaseDetails sscsCaseDetails = caseWithDocument("Some other document.txt");
        String documentPrefix = storeAppellantStatementService.documentNamePrefix(sscsCaseDetails, "onlineHearing");

        assertThat(documentPrefix, is("Appellant statement 1 - "));
    }

    @Test
    public void getDocumentPrefixWhenOtherStatements() {
        SscsCaseDetails sscsCaseDetails = caseWithDocument("Appellant statement 1 - SC0011111.pdf");
        String documentPrefix = storeAppellantStatementService.documentNamePrefix(sscsCaseDetails, "onlineHearing");

        assertThat(documentPrefix, is("Appellant statement 2 - "));
    }

    private SscsCaseDetails caseWithDocument(String documentFileName) {
        return SscsCaseDetails.builder().data(SscsCaseData.builder()
                .sscsDocument(singletonList(
                        SscsDocument.builder().value(
                                SscsDocumentDetails.builder()
                                        .documentFileName(documentFileName)
                                        .build())
                                .build()))
                .build()).build();
    }
}