package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfSummaryBuilder;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohConversation;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohConversations;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohRelisting;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class StoreOnlineHearingServiceTest {

    private static final String TITLE = "title";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String NINO = "nino";
    private static final String CASE_REF = "caseRef";
    private CohService cohService;
    private IdamService idamService;
    private CcdService ccdService;
    private PdfSummaryBuilder pdfSummaryBuilder;
    private EvidenceUploadService evidenceUploadService;
    private SscsPdfService sscsPdfService;
    private PdfService pdfService;
    private StoreOnlineHearingService underTest;
    private String hearingId;
    private long caseId;

    @Before
    public void setup() {
        cohService = mock(CohService.class);
        idamService = mock(IdamService.class);
        ccdService = mock(CcdService.class);
        pdfSummaryBuilder = mock(PdfSummaryBuilder.class);
        evidenceUploadService = mock(EvidenceUploadService.class);
        sscsPdfService = mock(SscsPdfService.class);
        pdfService = mock(PdfService.class);

        underTest = new StoreOnlineHearingService(
                cohService, idamService, ccdService, pdfSummaryBuilder,
                evidenceUploadService, sscsPdfService, pdfService
        );

        hearingId = "someOnlineHearingId";
        caseId = 123456L;
    }

    @Test
    public void storePdfInCcd() {
        CohConversations cohConversations = new CohConversations(new CohConversation(
                emptyList(),
                new CohRelisting("Relisting reason"))
        );
        when(cohService.getConversations(hearingId)).thenReturn(cohConversations);
        IdamTokens idamTokens = IdamTokens.builder().build();
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        SscsCaseDetails caseDetails = createCaseDetails();
        when(ccdService.getByCaseId(caseId, idamTokens)).thenReturn(caseDetails);

        PdfSummary pdfSummary = DataFixtures.somePdfSummary();
        when(pdfSummaryBuilder.buildPdfSummary(cohConversations, new PdfAppealDetails(TITLE, FIRST_NAME, LAST_NAME, NINO, CASE_REF)))
                .thenReturn(pdfSummary);
        byte[] pdf = {1, 2, 3};
        when(pdfService.createPdf(pdfSummary)).thenReturn(pdf);
        String fileName = "COR Transcript - " + CASE_REF + ".pdf";

        underTest.storeOnlineHearingInCcd(hearingId, caseId + "");

        verify(evidenceUploadService).uploadEvidence(Mockito.eq(caseId + ""), Mockito.any());
        verify(sscsPdfService).mergeDocIntoCcd(fileName, pdf, caseId, caseDetails.getData(), idamTokens);
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
                                        .build())
                                .build())
                        .generatedNino(NINO)
                        .caseReference(CASE_REF)
                        .build())
                .build();
    }

}