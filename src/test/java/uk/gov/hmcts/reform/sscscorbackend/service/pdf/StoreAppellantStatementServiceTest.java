package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.net.URI;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.hmcts.reform.sscs.ccd.domain.DocumentLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocumentDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnitParamsRunner.class)
@PrepareForTest(value = {StoreAppellantStatementService.class})
@PowerMockIgnore({"javax.net.ssl.*", "javax.security.*"})
public class StoreAppellantStatementServiceTest {

    private static final String APPELLANT_STATEMENT_1 = "Appellant statement 1 - ";
    private static final String APPELLANT_STATEMENT_1_1234_5678_9012_3456_PDF = "Appellant statement 1 - 1234-5678-9012-3456.pdf";
    private static final String APPELLANT_STATEMENT_2_1234_5678_9012_3456_PDF = "Appellant statement 2 - 1234-5678-9012-3456.pdf";
    private static final String OTHER_EVIDENCE = "Other evidence";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private PdfService pdfService;
    @Mock
    private CcdPdfService ccdPdfService;
    @Mock
    private IdamService idamService;
    @Mock
    private EvidenceManagementService evidenceManagementService;

    private StoreAppellantStatementService storeAppellantStatementService;

    @Before
    public void setUp() {
        storeAppellantStatementService = spy(new StoreAppellantStatementService(pdfService,
            "templatePath", ccdPdfService, idamService, evidenceManagementService));
    }

    @Test
    @Parameters(method = "generateDifferentCaseDataScenarios")
    public void givenCaseDetails_shouldWorkOutDocumentPrefix(SscsCaseData sscsCaseData, String expectedFileName) {
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(sscsCaseData).build();
        String documentPrefix = storeAppellantStatementService.documentNamePrefix(sscsCaseDetails,
            "onlineHearingId", null);

        assertThat(documentPrefix, is(expectedFileName));
    }

    private Object[] generateDifferentCaseDataScenarios() {
        SscsCaseData sscsCaseDataWithNoDocs = SscsCaseData.builder().build();
        SscsCaseData sscsCaseDataWithSomeOtherDoc = caseWithScannedDocumentAndSscsDocument(
            "Some other document.txt");
        SscsCaseData sscsCaseDataWithSomeOtherStatement = caseWithScannedDocumentAndSscsDocument(
            APPELLANT_STATEMENT_1_1234_5678_9012_3456_PDF);
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

    private SscsCaseData caseWithScannedDocumentAndSscsDocument(String scannedDocFilename) {
        return SscsCaseData.builder()
            .scannedDocuments(singletonList(ScannedDocument.builder()
                .value(ScannedDocumentDetails.builder()
                    .fileName(scannedDocFilename)
                    .url(DocumentLink.builder()
                        .documentUrl("http://dm-store/scannedDoc")
                        .build())
                    .build())
                .build()))
            .sscsDocument(singletonList(SscsDocument.builder()
                .value(SscsDocumentDetails.builder()
                    .documentFileName(StoreAppellantStatementServiceTest.APPELLANT_STATEMENT_2_1234_5678_9012_3456_PDF)
                    .documentLink(DocumentLink.builder()
                        .documentUrl("http://dm-store/sscsDoc")
                        .build())
                    .build())
                .build()))
            .build();
    }

    @Test
    public void givenCaseDataWithSomeOtherStatement_shouldCallTheStorePdfWithTheCorrectPdfName() {
        when(pdfService.createPdf(any(), eq("templatePath"))).thenReturn(new byte[0]);

        when(ccdPdfService.mergeDocIntoCcd(eq(APPELLANT_STATEMENT_2_1234_5678_9012_3456_PDF), any(),
            eq(1L), any(SscsCaseData.class), any(IdamTokens.class), eq(OTHER_EVIDENCE)))
            .thenReturn(SscsCaseData.builder().build());

        when(idamService.getIdamTokens()).thenReturn(IdamTokens.builder().build());

        SscsCaseDetails caseDetails = buildSscsCaseDetailsTestData();
        Statement statement = new Statement("some statement", "someAppealNumber");
        AppellantStatementPdfData data = new AppellantStatementPdfData(caseDetails, statement);

        storeAppellantStatementService.storePdf(1L, "onlineHearingId", data);

        ArgumentCaptor<String> acForPdfName = ArgumentCaptor.forClass(String.class);
        verify(ccdPdfService, times(1)).mergeDocIntoCcd(acForPdfName.capture(), any(),
            eq(1L), any(SscsCaseData.class), any(IdamTokens.class), eq(OTHER_EVIDENCE));
        assertThat(acForPdfName.getValue(), is(APPELLANT_STATEMENT_2_1234_5678_9012_3456_PDF));
        verifyZeroInteractions(evidenceManagementService);
    }

    @Test
    @Ignore
    //todo: make this test pass
    public void givenStatement_shouldStorePdfWithAppellantOrRepsInTheFileNameAccordingly() {
        when(pdfService.createPdf(any(), eq("templatePath"))).thenReturn(new byte[0]);

        when(ccdPdfService.mergeDocIntoCcd(eq("Representative statement 2 - 1234-5678-9012-3456.pdf"), any(),
            eq(1L), any(SscsCaseData.class), any(IdamTokens.class), eq(OTHER_EVIDENCE)))
            .thenReturn(SscsCaseData.builder().build());

        when(idamService.getIdamTokens()).thenReturn(IdamTokens.builder().build());

        SscsCaseDetails caseDetails = buildSscsCaseDetailsTestData();
        Statement statement = new Statement("some statement", "repsTyaCode");
        AppellantStatementPdfData data = new AppellantStatementPdfData(caseDetails, statement);

        storeAppellantStatementService.storePdf(1L, "onlineHearingId", data);

        ArgumentCaptor<String> acForPdfName = ArgumentCaptor.forClass(String.class);
        verify(ccdPdfService, times(1)).mergeDocIntoCcd(acForPdfName.capture(), any(),
            eq(1L), any(SscsCaseData.class), any(IdamTokens.class), eq(OTHER_EVIDENCE));
        assertThat(acForPdfName.getValue(), is("Representative statement 2 - 1234-5678-9012-3456.pdf"));
        verifyZeroInteractions(evidenceManagementService);
    }

    @Test
    public void givenCaseDataWithPdfStatementAlreadyCreated_shouldCallTheLoadPdf() throws Exception {
        doReturn(APPELLANT_STATEMENT_1_1234_5678_9012_3456_PDF).when(storeAppellantStatementService,
            "documentNamePrefix", any(SscsCaseDetails.class), anyString(),
            any(AppellantStatementPdfData.class));

        doReturn(false).when(storeAppellantStatementService,
            "pdfHasNotAlreadyBeenCreated", any(SscsCaseDetails.class), anyString());

        when(evidenceManagementService.download(eq(new URI("http://dm-store/scannedDoc")), anyString()))
            .thenReturn(new byte[0]);


        SscsCaseDetails caseDetails = buildSscsCaseDetailsTestData();
        Statement statement = new Statement("some statement", "someAppealNumber");
        AppellantStatementPdfData data = new AppellantStatementPdfData(caseDetails, statement);

        storeAppellantStatementService.storePdf(1L, "onlineHearingId", data);

        verify(evidenceManagementService, times(1))
            .download(eq(new URI("http://dm-store/scannedDoc")), anyString());
        verifyZeroInteractions(ccdPdfService);
        verifyZeroInteractions(idamService);
        verifyZeroInteractions(pdfService);
    }

    private SscsCaseDetails buildSscsCaseDetailsTestData() {
        SscsCaseData caseData = caseWithScannedDocumentAndSscsDocument(APPELLANT_STATEMENT_1_1234_5678_9012_3456_PDF
        );
        caseData.setCcdCaseId("1234-5678-9012-3456");
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