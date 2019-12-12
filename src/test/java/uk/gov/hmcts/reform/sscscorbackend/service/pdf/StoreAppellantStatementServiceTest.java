package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnitParamsRunner.class)
@PrepareForTest(value = {StorePdfService.class})
@PowerMockIgnore({ "javax.net.ssl.*", "javax.security.*" })
public class StoreAppellantStatementServiceTest {

    private static final String APPELLANT_STATEMENT_1 = "Appellant statement 1 - ";
    private static final String APPELLANT_STATEMENT_1_SC_0011111_PDF = "Appellant statement 1 - SC0011111.pdf";
    private static final String APPELLANT_STATEMENT_2_SC_0022222_PDF = "Appellant statement 2 - SC0022222.pdf";
    private static final String OTHER_EVIDENCE = "Other evidence";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private PdfService pdfService;
    @Mock
    private CcdPdfService ccdPdfService;
    @Mock
    private IdamService idamService;

    private StoreAppellantStatementService storeAppellantStatementService;

    @Before
    public void setUp() {
        storeAppellantStatementService = new StoreAppellantStatementService(pdfService, "templatePath",
            ccdPdfService, idamService, mock(EvidenceManagementService.class));
    }

    @Test
    @Parameters(method = "generateDifferentCaseDataScenarios")
    public void givenCaseDetails_shouldWorkOutDocumentPrefix(SscsCaseData sscsCaseData, String expectedFileName) {
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(sscsCaseData).build();
        String documentPrefix = storeAppellantStatementService.documentNamePrefix(sscsCaseDetails,
            "onlineHearingId");

        assertThat(documentPrefix, is(expectedFileName));
    }

    private Object[] generateDifferentCaseDataScenarios() {
        SscsCaseData sscsCaseDataWithNoDocs = SscsCaseData.builder().build();
        SscsCaseData sscsCaseDataWithSomeOtherDoc = caseWithDocument("Some other document.txt");
        SscsCaseData sscsCaseDataWithSomeOtherStatement = caseWithDocument(APPELLANT_STATEMENT_1_SC_0011111_PDF);
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
            new Object[]{sscsCaseDataWithNoDocs, APPELLANT_STATEMENT_1},
            new Object[]{sscsCaseDataWithSomeOtherDoc, APPELLANT_STATEMENT_1},
            new Object[]{sscsCaseDataWithSomeOtherStatement, "Appellant statement 2 - "},
            new Object[]{sscsCaseDataWithDocWithNullValue, APPELLANT_STATEMENT_1},
            new Object[]{sscsCaseDataWithDocWithEmptyFilename, APPELLANT_STATEMENT_1},
            new Object[]{sscsCaseDataWithDocWithNullFilename, APPELLANT_STATEMENT_1}
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

    @Test
    public void givenCaseDataWithSomeOtherStatement_shouldCallTheStorePdfWithTheCorrectPdfName() {

        when(pdfService.createPdf(any(), eq("templatePath"))).thenReturn(new byte[0]);

        when(ccdPdfService.mergeDocIntoCcd(eq(APPELLANT_STATEMENT_2_SC_0022222_PDF), any(),
            eq(1L), any(SscsCaseData.class), any(IdamTokens.class), eq(OTHER_EVIDENCE)))
            .thenReturn(SscsCaseData.builder().build());

        when(idamService.getIdamTokens()).thenReturn(IdamTokens.builder().build());

        SscsCaseDetails caseDetails = buildSscsCaseDetailsTestData();
        Statement statement = new Statement("some statement");
        AppellantStatementPdfData data = new AppellantStatementPdfData(caseDetails, statement);

        storeAppellantStatementService.storePdf(1L, "onlineHearingId", data);

        ArgumentCaptor<String> acForPdfName = ArgumentCaptor.forClass(String.class);
        verify(ccdPdfService, times(1)).mergeDocIntoCcd(acForPdfName.capture(), any(),
            eq(1L), any(SscsCaseData.class), any(IdamTokens.class), eq(OTHER_EVIDENCE));

        assertThat(acForPdfName.getValue(), is(APPELLANT_STATEMENT_2_SC_0022222_PDF));
    }

    private SscsCaseDetails buildSscsCaseDetailsTestData() {
        SscsCaseData caseData = caseWithDocument(APPELLANT_STATEMENT_1_SC_0011111_PDF);
        caseData.setAppeal(Appeal.builder()
            .appellant(Appellant.builder()
                .name(Name.builder()
                    .title("Mr")
                    .firstName("firstName")
                    .lastName("lastName")
                    .build())
                .identity(Identity.builder()
                    .nino("ab123456c")
                    .build())
                .build())
            .build());
        caseData.setCaseReference("SC0022222");

        return SscsCaseDetails.builder()
            .data(caseData)
            .build();
    }
}