package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;

@Slf4j
@Service
public class StoreOnlineHearingTribunalsViewService {

    public static final String TRIBUNALS_VIEW_PDF_PREFIX = "Tribunals view - ";
    private final OnlineHearingService onlineHearingService;
    private final PdfService pdfService;
    private final OnlineHearingDateReformatter onlineHearingDateReformatter;
    private final SscsPdfService sscsPdfService;
    private final CcdService ccdService;
    private final IdamService idamService;

    public StoreOnlineHearingTribunalsViewService(OnlineHearingService onlineHearingService,
                                                  @Qualifier("PreliminaryViewPdfService") PdfService pdfService,
                                                  OnlineHearingDateReformatter onlineHearingDateReformatter,
                                                  SscsPdfService sscsPdfService, CcdService ccdService, IdamService idamService) {
        this.onlineHearingService = onlineHearingService;
        this.pdfService = pdfService;
        this.onlineHearingDateReformatter = onlineHearingDateReformatter;
        this.sscsPdfService = sscsPdfService;
        this.ccdService = ccdService;
        this.idamService = idamService;
    }

    public void storeTribunalsView(Long caseId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);
        if (doesNotHaveATribunalView(caseDetails)) {
            Optional<OnlineHearing> optioanlOnlineHearing = onlineHearingService.loadOnlineHearingFromCoh(caseDetails);
            OnlineHearing onlineHearing = optioanlOnlineHearing.orElseThrow(() -> new IllegalArgumentException("Cannot find online hearing for case id [" + caseId + "]"));
            byte[] pdfBytes = pdfService.createPdf(onlineHearingDateReformatter.getReformattedOnlineHearing(onlineHearing));
            SscsCaseData caseData = caseDetails.getData();
            sscsPdfService.mergeDocIntoCcd(TRIBUNALS_VIEW_PDF_PREFIX + caseData.getCaseReference() + ".pdf", pdfBytes, caseId, caseData, idamTokens);
        }
    }

    private boolean doesNotHaveATribunalView(SscsCaseDetails caseDetails) {
        return caseDetails.getData().getSscsDocument().stream().noneMatch(
                sscsDocument -> sscsDocument.getValue().getDocumentFileName().startsWith(TRIBUNALS_VIEW_PDF_PREFIX)
        );
    }
}
