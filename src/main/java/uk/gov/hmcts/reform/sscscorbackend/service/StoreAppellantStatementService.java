package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppellantStatement;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
public class StoreAppellantStatementService extends StorePdfService<PdfAppellantStatement, AppellantStatementPdfData> {

    private static final String FILE_NAME_PREFIX = "Appellant statement ";

    @Autowired
    public StoreAppellantStatementService(
            @Qualifier("oldPdfService") PdfService pdfService,
            @Value("${personalStatement.html.template.path}")String pdfTemplatePath,
            CcdPdfService ccdPdfService,
            IdamService idamService,
            EvidenceManagementService evidenceManagementService) {
        super(pdfService, pdfTemplatePath, ccdPdfService, idamService, evidenceManagementService);
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        List<SscsDocument> sscsDocuments = caseDetails.getData().getSscsDocument();

        long numberOfAppellantStatements = sscsDocuments != null
                ? sscsDocuments.stream().filter(sscsDocument -> sscsDocument.getValue().getDocumentFileName().startsWith(FILE_NAME_PREFIX)).count()
                : 0;

        long appellantStatementNumber = numberOfAppellantStatements + 1;
        return FILE_NAME_PREFIX + appellantStatementNumber + " - ";
    }

    @Override
    protected PdfAppellantStatement getPdfContent(AppellantStatementPdfData data, String onlineHearingId, PdfAppealDetails appealDetails) {
        return new PdfAppellantStatement(appealDetails, data.getStatement().getBody());
    }
}
