package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class StorePdfServiceTest {
    private static final String TITLE = "title";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String NINO = "nino";
    private static final String CASE_REF = "caseRef";


    private PdfService pdfService;
    private SscsPdfService sscsPdfService;
    private long caseId;
    private Object pdfContent;
    private String fileNamePrefix;
    private StorePdfService storePdfService;
    private IdamTokens idamTokens;
    private String someOnlineHearingId;
    private EvidenceManagementService evidenceManagementService;

    @Before
    public void setUp() {
        pdfService = mock(PdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        IdamService idamService = mock(IdamService.class);
        idamTokens = IdamTokens.builder().build();
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        caseId = 123L;
        pdfContent = new Object();
        fileNamePrefix = "test name";
        evidenceManagementService = mock(EvidenceManagementService.class);
        storePdfService = new StorePdfService(pdfService, "sometemplate", sscsPdfService, idamService, evidenceManagementService) {

            @Override
            protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
                return fileNamePrefix;
            }

            @Override
            protected Object getPdfContent(SscsCaseDetails caseDetails, String onlineHearingId, PdfAppealDetails appealDetails) {
                return pdfContent;
            }
        };
        someOnlineHearingId = "someOnlineHearingId";
    }

    @Test
    public void storePdf() {
        SscsCaseDetails caseDetails = createCaseDetails();
        byte[] expectedPdfBytes = {2, 4, 6, 0, 1};
        when(pdfService.createPdf(pdfContent, "sometemplate")).thenReturn(expectedPdfBytes);

        CohEventActionContext cohEventActionContext = storePdfService.storePdf(caseId, someOnlineHearingId, caseDetails);

        verify(sscsPdfService).mergeDocIntoCcd(fileNamePrefix + CASE_REF + ".pdf", expectedPdfBytes, caseId, caseDetails.getData(), idamTokens);
        assertThat(cohEventActionContext.getPdf().getContent(), is(expectedPdfBytes));
        assertThat(cohEventActionContext.getPdf().getName(), is(fileNamePrefix + CASE_REF + ".pdf"));
        assertThat(cohEventActionContext.getDocument(), is(caseDetails));
    }

    @Test
    public void doNotStorePdfIfCaseAlreadyHasATribunalsView() throws URISyntaxException {
        String documentUrl = "http://example.com/someDocument";
        SscsCaseData sscsCaseData = SscsCaseData.builder()
                .caseReference(CASE_REF)
                .generatedNino("someNino")
                .sscsDocument(singletonList(SscsDocument.builder()
                        .value(SscsDocumentDetails.builder()
                                .documentFileName(fileNamePrefix + CASE_REF + ".pdf")
                                .documentLink(DocumentLink.builder().documentUrl(documentUrl).build())
                                .build())
                        .build()))
                .appeal(Appeal.builder().appellant(Appellant.builder().name(Name.builder()
                        .title("Mr")
                        .firstName("Jean")
                        .lastName("Valjean")
                        .build()
                ).build()).build()).build();
        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(sscsCaseData).build();
        byte[] expectedPdfBytes = {2, 4, 6, 0, 1};
        when(evidenceManagementService.download(new URI(documentUrl), "sscs")).thenReturn(expectedPdfBytes);

        CohEventActionContext cohEventActionContext = storePdfService.storePdf(caseId, someOnlineHearingId, sscsCaseDetails);

        verifyZeroInteractions(sscsPdfService);
        assertThat(cohEventActionContext.getPdf().getContent(), is(expectedPdfBytes));
        assertThat(cohEventActionContext.getPdf().getName(), is(fileNamePrefix + CASE_REF + ".pdf"));
        assertThat(cohEventActionContext.getDocument(), is(sscsCaseDetails));
    }

    private SscsCaseDetails createCaseDetails() {
        return SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .appeal(Appeal.builder()
                                .appellant(Appellant.builder()
                                        .name(Name.builder()
                                                .title(TITLE)
                                                .firstName(FIRST_NAME)
                                                .lastName(LAST_NAME)
                                                .build())
                                        .identity(Identity.builder()
                                                .nino(NINO)
                                                .build())
                                        .build())
                                .build())
                        .caseReference(CASE_REF)
                        .build())
                .build();
    }
}