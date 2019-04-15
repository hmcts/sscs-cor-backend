package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfSummaryBuilder;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohConversations;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.OldPdfService;

public class StoreOnlineHearingServiceTest {

    private static final String TITLE = "title";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String NINO = "nino";
    private static final String CASE_REF = "caseRef";
    private CohService cohService;
    private PdfSummaryBuilder pdfSummaryBuilder;
    private StoreOnlineHearingService underTest;

    @Before
    public void setup() {
        cohService = mock(CohService.class);
        pdfSummaryBuilder = mock(PdfSummaryBuilder.class);

        underTest = new StoreOnlineHearingService(
                cohService, mock(IdamService.class), pdfSummaryBuilder,
                mock(CcdPdfService.class), mock(OldPdfService.class), "sometemplate",
                mock(EvidenceManagementService.class));
    }

    @Test
    public void canCreatePdfSummary() {
        String someOnlineHearingId = "someOnlineHearingId";
        CohConversations conversations = mock(CohConversations.class);
        PdfAppealDetails pdfAppealDetails = mock(PdfAppealDetails.class);
        when(cohService.getConversations(someOnlineHearingId)).thenReturn(conversations);
        PdfSummary expectedPdfSummary = mock(PdfSummary.class);
        when(pdfSummaryBuilder.buildPdfSummary(conversations, pdfAppealDetails)).thenReturn(expectedPdfSummary);

        PdfSummary pdfSummary = underTest.getPdfContent(mock(PdfData.class), someOnlineHearingId, pdfAppealDetails);

        assertThat(pdfSummary, is(expectedPdfSummary));
    }
}