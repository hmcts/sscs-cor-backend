package uk.gov.hmcts.reform.sscscorbackend.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.Pdf;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
public abstract class StorePdfService<E, D extends PdfData> {
    private final PdfService pdfService;
    private final String pdfTemplatePath;
    private final CcdPdfService ccdPdfService;
    private final IdamService idamService;
    private final EvidenceManagementService evidenceManagementService;

    StorePdfService(PdfService pdfService,
                    String pdfTemplatePath,
                    CcdPdfService ccdPdfService,
                    IdamService idamService,
                    EvidenceManagementService evidenceManagementService) {
        this.pdfService = pdfService;
        this.pdfTemplatePath = pdfTemplatePath;
        this.ccdPdfService = ccdPdfService;
        this.idamService = idamService;
        this.evidenceManagementService = evidenceManagementService;
    }

    public CohEventActionContext storePdf(Long caseId, String onlineHearingId, D data) {
        SscsCaseDetails caseDetails = data.getCaseDetails();
        String documentNamePrefix = documentNamePrefix(caseDetails, onlineHearingId);
        if (pdfHasNotAlreadyBeenCreated(caseDetails, documentNamePrefix)) {
            log.info("Creating pdf for [" + caseId + "]");
            return new CohEventActionContext(
                    storePdf(caseId, onlineHearingId, idamService.getIdamTokens(), data, documentNamePrefix),
                    caseDetails
            );
        } else {
            log.info("Loading pdf for [" + caseId + "]");
            return new CohEventActionContext(loadPdf(caseDetails, documentNamePrefix), caseDetails);
        }
    }

    private Pdf storePdf(Long caseId, String onlineHearingId, IdamTokens idamTokens, D data, String documentNamePrefix) {
        SscsCaseDetails caseDetails = data.getCaseDetails();
        PdfAppealDetails pdfAppealDetails = getPdfAppealDetails(caseId, caseDetails);
        log.info("Storing pdf for [" + caseId + "]");
        byte[] pdfBytes = pdfService.createPdf(getPdfContent(data, onlineHearingId, pdfAppealDetails), pdfTemplatePath);

        SscsCaseData caseData = caseDetails.getData();
        String pdfName = getPdfName(documentNamePrefix, caseData.getCaseReference());
        log.info("Adding pdf to ccd for [" + caseId + "]");
        ccdPdfService.mergeDocIntoCcd(pdfName, pdfBytes, caseId, caseData, idamTokens);

        return new Pdf(pdfBytes, pdfName);
    }

    private String getPdfName(String documentNamePrefix, String caseReference) {
        return documentNamePrefix + caseReference + ".pdf";
    }

    private Pdf loadPdf(SscsCaseDetails caseDetails, String documentNamePrefix) {
        SscsDocument document = caseDetails.getData().getSscsDocument().stream()
                .filter(sscsDocument -> sscsDocument.getValue().getDocumentFileName() != null)
                .filter(documentNameMatches(documentNamePrefix))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Found PDF with name prefix [" + documentNamePrefix + "] but cannot load it"));
        String documentUrl = document.getValue().getDocumentLink().getDocumentUrl();
        try {
            return new Pdf(evidenceManagementService.download(new URI(documentUrl), "sscs"), getPdfName(documentNamePrefix, caseDetails.getData().getCaseReference()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Document uri invalid [" + documentUrl + "]");
        }
    }

    private boolean pdfHasNotAlreadyBeenCreated(SscsCaseDetails caseDetails, String documentNamePrefix) {
        List<SscsDocument> sscsDocuments = caseDetails.getData().getSscsDocument();
        return sscsDocuments == null || sscsDocuments.stream()
                .filter(sscsDocument -> sscsDocument.getValue().getDocumentFileName() != null)
                .noneMatch(documentNameMatches(documentNamePrefix));
    }

    private Predicate<SscsDocument> documentNameMatches(String documentNamePrefix) {
        return sscsDocument -> sscsDocument.getValue().getDocumentFileName().startsWith(documentNamePrefix);
    }

    private PdfAppealDetails getPdfAppealDetails(Long caseId, SscsCaseDetails caseDetails) {
        log.info("Got case details for {}", caseId);
        String appellantTitle = caseDetails.getData().getAppeal().getAppellant().getName().getTitle();
        String appellantFirstName = caseDetails.getData().getAppeal().getAppellant().getName().getFirstName();
        String appellantLastName = caseDetails.getData().getAppeal().getAppellant().getName().getLastName();

        String nino = caseDetails.getData().getAppeal().getAppellant().getIdentity().getNino();
        String caseReference = caseDetails.getData().getCaseReference();

        return new PdfAppealDetails(appellantTitle, appellantFirstName, appellantLastName, nino, caseReference);
    }

    protected abstract String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId);

    protected abstract E getPdfContent(D data, String onlineHearingId, PdfAppealDetails appealDetails);
}
