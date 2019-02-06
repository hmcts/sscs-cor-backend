package uk.gov.hmcts.reform.sscscorbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfSummaryBuilder;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohConversations;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
@Service
public class StoreOnlineHearingService extends BasePdfService<PdfSummary> {
    private final CohService cohService;
    private final PdfSummaryBuilder pdfSummaryBuilder;

    public StoreOnlineHearingService(CohService cohService,
                                     IdamService idamService,
                                     CcdService ccdService,
                                     PdfSummaryBuilder pdfSummaryBuilder,
                                     SscsPdfService sscsPdfService,
                                     PdfService pdfService,
                                     @Value("${online_hearing_finished.html.template.path}") String templatePath,
                                     EvidenceManagementService evidenceManagementService) {
        super(pdfService, templatePath, sscsPdfService, ccdService, idamService, evidenceManagementService);
        this.cohService = cohService;
        this.pdfSummaryBuilder = pdfSummaryBuilder;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        return "COR Transcript - ";
    }

    @Override
    protected PdfSummary getPdfContent(SscsCaseDetails caseDetails, String onlineHearingId, PdfAppealDetails appealDetails) {
        CohConversations conversations = cohService.getConversations(onlineHearingId);
        return pdfSummaryBuilder.buildPdfSummary(conversations, appealDetails);

    }
}
