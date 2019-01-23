package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
@Service
public class StoreOnlineHearingTribunalsViewService extends BasePdfService<OnlineHearing> {

    public static final String TRIBUNALS_VIEW_PDF_PREFIX = "Tribunals view - ";
    private final OnlineHearingService onlineHearingService;
    private final OnlineHearingDateReformatter onlineHearingDateReformatter;

    public StoreOnlineHearingTribunalsViewService(OnlineHearingService onlineHearingService,
                                                  @Qualifier("PreliminaryViewPdfService") PdfService pdfService,
                                                  OnlineHearingDateReformatter onlineHearingDateReformatter,
                                                  SscsPdfService sscsPdfService, CcdService ccdService, IdamService idamService,
                                                  EvidenceManagementService evidenceManagementService) {
        super(pdfService, sscsPdfService, ccdService, idamService, evidenceManagementService);
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingDateReformatter = onlineHearingDateReformatter;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        return TRIBUNALS_VIEW_PDF_PREFIX;
    }

    @Override
    protected OnlineHearing getPdfContent(SscsCaseDetails caseDetails, String onlineHearingId, PdfAppealDetails appealDetails) {
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.loadOnlineHearingFromCoh(caseDetails);
        OnlineHearing onlineHearing = optionalOnlineHearing.orElseThrow(() -> new IllegalArgumentException("Cannot find online hearing for case id [" + caseDetails.getId() + "]"));

        return onlineHearingDateReformatter.getReformattedOnlineHearing(onlineHearing);
    }
}
