package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfEvidenceDescription;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.EvidenceDescriptionPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
public class StoreEvidenceDescriptionService extends StorePdfService<PdfEvidenceDescription, EvidenceDescriptionPdfData> {
    StoreEvidenceDescriptionService(
            @Qualifier("oldPdfService") PdfService pdfService,
            @Value("${evidenceDescription.html.template.path}")String pdfTemplatePath,
            CcdPdfService ccdPdfService,
            IdamService idamService,
            EvidenceManagementService evidenceManagementService) {
        super(pdfService, pdfTemplatePath, ccdPdfService, idamService, evidenceManagementService);
    }

    @Override
    protected boolean pdfHasNotAlreadyBeenCreated(SscsCaseDetails caseDetails, String documentNamePrefix) {
        return true;
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId,
                                        EvidenceDescriptionPdfData data) {
        return "Evidence Description - ";
    }

    @Override
    protected PdfEvidenceDescription getPdfContent(EvidenceDescriptionPdfData data, String onlineHearingId,
                                                   PdfAppealDetails appealDetails) {
        return new PdfEvidenceDescription(appealDetails, data.getDescription().getBody(), data.getFileNames());
    }
}
