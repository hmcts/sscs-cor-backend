package uk.gov.hmcts.reform.sscscorbackend.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscs.service.SscsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
public abstract class BasePdfService<E> {
    private final PdfService pdfService;
    private final SscsPdfService sscsPdfService;
    private final CcdService ccdService;
    private final IdamService idamService;
    private final EvidenceManagementService evidenceManagementService;

    BasePdfService(PdfService pdfService,
                   SscsPdfService sscsPdfService,
                   CcdService ccdService,
                   IdamService idamService,
                   EvidenceManagementService evidenceManagementService) {
        this.pdfService = pdfService;
        this.sscsPdfService = sscsPdfService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.evidenceManagementService = evidenceManagementService;
    }

    public byte[] storePdf(Long caseId, String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);
        if (pdfHasNotAlreadyBeenCreated(caseDetails)) {
            log.info("Creating pdf for [" + caseId + "]");
            return storePdf(caseId, onlineHearingId, idamTokens, caseDetails);
        } else {
            log.info("Loading pdf for [" + caseId + "]");
            return loadPdf(caseDetails);
        }
    }

    private byte[] storePdf(Long caseId, String onlineHearingId, IdamTokens idamTokens, SscsCaseDetails caseDetails) {
        PdfAppealDetails pdfAppealDetails = getPdfAppealDetails(caseId, caseDetails);
        byte[] pdfBytes = pdfService.createPdf(getPdfContent(caseDetails, onlineHearingId, pdfAppealDetails));
        SscsCaseData caseData = caseDetails.getData();
        sscsPdfService.mergeDocIntoCcd(documentNameStartsWith() + caseData.getCaseReference() + ".pdf", pdfBytes, caseId, caseData, idamTokens);

        return pdfBytes;
    }

    private byte[] loadPdf(SscsCaseDetails caseDetails) {
        SscsDocument document = caseDetails.getData().getSscsDocument().stream()
                .filter(sscsDocument -> sscsDocument.getValue().getDocumentFileName().startsWith(documentNameStartsWith()))
                .findFirst().get();
        String documentUrl = document.getValue().getDocumentLink().getDocumentUrl();
        try {
            return evidenceManagementService.download(new URI(documentUrl), "sscs");
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Document uri invalid [" + documentUrl + "]");
        }
    }

    private boolean pdfHasNotAlreadyBeenCreated(SscsCaseDetails caseDetails) {
        List<SscsDocument> sscsDocuments = caseDetails.getData().getSscsDocument();
        return sscsDocuments == null || sscsDocuments.stream()
                .noneMatch(sscsDocument -> sscsDocument.getValue().getDocumentFileName().startsWith(documentNameStartsWith()));
    }

    private PdfAppealDetails getPdfAppealDetails(Long caseId, SscsCaseDetails caseDetails) {
        log.info("Got case details for {}", caseId);
        String appellantTitle = caseDetails.getData().getAppeal().getAppellant().getName().getTitle();
        String appellantFirstName = caseDetails.getData().getAppeal().getAppellant().getName().getFirstName();
        String appellantLastName = caseDetails.getData().getAppeal().getAppellant().getName().getLastName();

        String nino = caseDetails.getData().getGeneratedNino();
        String caseReference = caseDetails.getData().getCaseReference();

        return new PdfAppealDetails(appellantTitle, appellantFirstName, appellantLastName, nino, caseReference);
    }

    protected abstract String documentNameStartsWith();

    protected abstract E getPdfContent(SscsCaseDetails caseDetails, String onlineHearingId, PdfAppealDetails appealDetails);
}
