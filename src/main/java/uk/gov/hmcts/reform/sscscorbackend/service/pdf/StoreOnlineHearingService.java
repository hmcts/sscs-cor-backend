package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohConversations;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
@Service
public class StoreOnlineHearingService extends StorePdfService<PdfSummary, PdfData> {
    private final CohService cohService;
    private final PdfSummaryBuilder pdfSummaryBuilder;

    @SuppressWarnings("squid:S00107")
    public StoreOnlineHearingService(CohService cohService,
                                     IdamService idamService,
                                     PdfSummaryBuilder pdfSummaryBuilder,
                                     CcdPdfService ccdPdfService,
                                     @Qualifier("oldPdfService") PdfService pdfService,
                                     @Value("${online_hearing_finished.html.template.path}") String templatePath,
                                     EvidenceManagementService evidenceManagementService) {
        super(pdfService, templatePath, ccdPdfService, idamService, evidenceManagementService);
        this.cohService = cohService;
        this.pdfSummaryBuilder = pdfSummaryBuilder;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId, PdfData data) {
        return "COR Transcript - ";
    }

    @Override
    protected PdfSummary getPdfContent(PdfData caseDetails, String onlineHearingId, PdfAppealDetails appealDetails) {
        CohConversations conversations = cohService.getConversations(onlineHearingId);
        return pdfSummaryBuilder.buildPdfSummary(conversations, appealDetails);

    }
}
