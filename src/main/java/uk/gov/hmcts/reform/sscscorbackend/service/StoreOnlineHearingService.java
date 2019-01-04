package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.springframework.http.MediaType.APPLICATION_PDF;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.domain.pdf.ByteArrayMultipartFile;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohConversations;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;

@Slf4j
@Service
public class StoreOnlineHearingService {
    private final CohService cohService;
    private final IdamService idamService;
    private final CcdService ccdService;
    private final PdfSummaryBuilder pdfSummaryBuilder;
    private final EvidenceUploadService evidenceUploadService;
    private final SscsPdfService sscsPdfService;
    private final PdfService pdfService;

    public StoreOnlineHearingService(CohService cohService,
                                     IdamService idamService,
                                     CcdService ccdService,
                                     PdfSummaryBuilder pdfSummaryBuilder,
                                     EvidenceUploadService evidenceUploadService,
                                     SscsPdfService sscsPdfService,
                                     @Qualifier("QuestionAnswerPdfService") PdfService pdfService) {
        this.cohService = cohService;
        this.idamService = idamService;
        this.ccdService = ccdService;
        this.pdfSummaryBuilder = pdfSummaryBuilder;
        this.evidenceUploadService = evidenceUploadService;
        this.sscsPdfService = sscsPdfService;
        this.pdfService = pdfService;
    }

    public void storeOnlineHearingInCcd(String onlineHearingId, String caseId) {
        CohConversations conversations = cohService.getConversations(onlineHearingId);

        log.info("Got question rounds for hearing {}", onlineHearingId);

        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseDetails caseDetails = ccdService.getByCaseId(Long.valueOf(caseId), idamTokens);

        PdfAppealDetails appealDetails = getPdfAppealDetails(caseId, caseDetails);
        PdfSummary pdfSummary = pdfSummaryBuilder.buildPdfSummary(conversations, appealDetails);
        byte[] pdfBytes = pdfService.createPdf(pdfSummary);
        storePdf(onlineHearingId, caseId, idamTokens, caseDetails, appealDetails, pdfBytes);
    }

    private void storePdf(String onlineHearingId, String caseId, IdamTokens idamTokens, SscsCaseDetails caseDetails, PdfAppealDetails appealDetails, byte[] pdfBytes) {
        String fileName = "COR Transcript - " + appealDetails.getCaseReference() + ".pdf";
        ByteArrayMultipartFile file = ByteArrayMultipartFile.builder().content(pdfBytes).name(fileName).contentType(APPLICATION_PDF).build();
        log.info("Creating transcript file {} for hearing {}", fileName, onlineHearingId);

        evidenceUploadService.uploadEvidence(caseId, file); // Do we need to do this, is the call below also not adding the file to doc store?

        sscsPdfService.mergeDocIntoCcd(fileName, pdfBytes, Long.valueOf(caseId), caseDetails.getData(), idamTokens);
    }

    private PdfAppealDetails getPdfAppealDetails(String caseId, SscsCaseDetails caseDetails) {
        log.info("Got case details for {}", caseId);
        String appellantTitle = caseDetails.getData().getAppeal().getAppellant().getName().getTitle();
        String appellantFirstName = caseDetails.getData().getAppeal().getAppellant().getName().getFirstName();
        String appellantLastName = caseDetails.getData().getAppeal().getAppellant().getName().getLastName();

        String nino = caseDetails.getData().getGeneratedNino();
        String caseReference = caseDetails.getData().getCaseReference();

        return new PdfAppealDetails(appellantTitle, appellantFirstName, appellantLastName, nino, caseReference);
    }
}
