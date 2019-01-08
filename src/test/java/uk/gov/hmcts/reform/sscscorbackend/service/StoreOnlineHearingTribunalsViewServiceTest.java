package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService.TRIBUNALS_VIEW_PDF_PREFIX;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;

public class StoreOnlineHearingTribunalsViewServiceTest {

    private final String someCaseReference = "CaseReference";
    private final Long someCaseId = 123456L;
    private OnlineHearingService onlineHearingService;
    private OnlineHearingDateReformatter onlineHearingDateReformatter;
    private PdfService pdfService;
    private StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private SscsPdfService sscsPdfService;
    private CcdService ccdService;
    private IdamService idamService;
    private IdamTokens idamTokens;
    private SscsCaseDetails sscsCaseDetails;
    private SscsCaseData sscsCaseData;

    @Before
    public void setUp() {
        onlineHearingService = mock(OnlineHearingService.class);
        onlineHearingDateReformatter = mock(OnlineHearingDateReformatter.class);
        pdfService = mock(PdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        sscsPdfService = mock(SscsPdfService.class);
        idamService = mock(IdamService.class);
        ccdService = mock(CcdService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        storeOnlineHearingTribunalsViewService = new StoreOnlineHearingTribunalsViewService(
                onlineHearingService,
                pdfService,
                onlineHearingDateReformatter,
                sscsPdfService,
                ccdService,
                idamService);
        sscsCaseDetails = mock(SscsCaseDetails.class);
        sscsCaseData = mock(SscsCaseData.class);
        when(sscsCaseDetails.getData()).thenReturn(sscsCaseData);
        when(sscsCaseData.getCaseReference()).thenReturn(someCaseReference);
        when(ccdService.getByCaseId(someCaseId, idamTokens)).thenReturn(sscsCaseDetails);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfCannotFindHearingId() {
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetails)).thenReturn(Optional.empty());
        storeOnlineHearingTribunalsViewService.storeTribunalsView(someCaseId);
    }

    @Test
    public void createsPdf() {
        Optional<OnlineHearing> onlineHearing = Optional.of(mock(OnlineHearing.class));
        when(onlineHearingService.loadOnlineHearingFromCoh(sscsCaseDetails)).thenReturn(onlineHearing);
        OnlineHearing reformattedOnlineHearing = mock(OnlineHearing.class);
        when(onlineHearingDateReformatter.getReformattedOnlineHearing(onlineHearing.get())).thenReturn(reformattedOnlineHearing);
        byte[] pdfBytes = {1, 2, 3};
        when(pdfService.createPdf(reformattedOnlineHearing)).thenReturn(pdfBytes);

        storeOnlineHearingTribunalsViewService.storeTribunalsView(someCaseId);

        String fileName = "Tribunals view - " + someCaseReference + ".pdf";
        verify(sscsPdfService).mergeDocIntoCcd(fileName, pdfBytes, someCaseId, sscsCaseData, idamTokens);
    }

    @Test
    public void doNotStorePdfIfCaseAlreadyHasATribunalsView() {
        when(sscsCaseData.getSscsDocument()).thenReturn(singletonList(SscsDocument.builder().value(
                SscsDocumentDetails.builder().documentFileName(TRIBUNALS_VIEW_PDF_PREFIX + someCaseId + ".pdf").build()
        ).build()));

        storeOnlineHearingTribunalsViewService.storeTribunalsView(someCaseId);

        verifyZeroInteractions(sscsPdfService);
    }
}