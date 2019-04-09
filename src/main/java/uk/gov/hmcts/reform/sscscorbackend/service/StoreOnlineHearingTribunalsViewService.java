package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
@Service
public class StoreOnlineHearingTribunalsViewService extends StorePdfService<OnlineHearing, PdfData> {

    public static final String TRIBUNALS_VIEW_PDF_PREFIX = "Tribunals view - ";
    private final OnlineHearingService onlineHearingService;
    private final OnlineHearingDateReformatter onlineHearingDateReformatter;
    private final ActivitiesValidator activitiesValidator;

    @SuppressWarnings("squid:S00107")
    public StoreOnlineHearingTribunalsViewService(OnlineHearingService onlineHearingService,
                                                  @Qualifier("oldPdfService") PdfService pdfService,
                                                  @Value("${preliminary_view.html.template.path}") String templatePath,
                                                  OnlineHearingDateReformatter onlineHearingDateReformatter,
                                                  CcdPdfService ccdPdfService, IdamService idamService,
                                                  EvidenceManagementService evidenceManagementService,
                                                  ActivitiesValidator activitiesValidator) {
        super(pdfService, templatePath, ccdPdfService, idamService, evidenceManagementService);
        this.onlineHearingService = onlineHearingService;
        this.onlineHearingDateReformatter = onlineHearingDateReformatter;
        this.activitiesValidator = activitiesValidator;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        return TRIBUNALS_VIEW_PDF_PREFIX;
    }

    @Override
    protected OnlineHearing getPdfContent(PdfData data, String onlineHearingId, PdfAppealDetails appealDetails) {
        SscsCaseDetails caseDetails = data.getCaseDetails();
        Optional<OnlineHearing> optionalOnlineHearing = onlineHearingService.loadOnlineHearingFromCoh(caseDetails);
        OnlineHearing onlineHearing = optionalOnlineHearing.orElseThrow(() -> new IllegalArgumentException("Cannot find online hearing for case id [" + caseDetails.getId() + "]"));

        activitiesValidator.validateWeHaveMappingForActivities(onlineHearing);

        return onlineHearingDateReformatter.getReformattedOnlineHearing(onlineHearing);
    }
}
