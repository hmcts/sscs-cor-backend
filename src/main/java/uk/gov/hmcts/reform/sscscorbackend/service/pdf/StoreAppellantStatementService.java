package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppellantStatement;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Slf4j
@Service
public class StoreAppellantStatementService extends StorePdfService<PdfAppellantStatement, AppellantStatementPdfData> {

    private static final String FILE_NAME_PREFIX = "Appellant statement ";

    @Autowired
    public StoreAppellantStatementService(
        @Qualifier("oldPdfService") PdfService pdfService,
        @Value("${personalStatement.html.template.path}") String pdfTemplatePath,
        CcdPdfService ccdPdfService,
        IdamService idamService,
        EvidenceManagementService evidenceManagementService) {
        super(pdfService, pdfTemplatePath, ccdPdfService, idamService, evidenceManagementService);
    }

    @Override
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId) {
        List<ScannedDocument> scannedDocuments = caseDetails.getData().getScannedDocuments();
        List<SscsDocument> sscsDocuments = caseDetails.getData().getSscsDocument();

        long numberOfAppellantStatements = scannedDocuments != null
            ? getCountOfAppellantStatements(scannedDocuments, sscsDocuments) : 0;

        long appellantStatementNumber = numberOfAppellantStatements + 1;
        return FILE_NAME_PREFIX + appellantStatementNumber + " - ";
    }

    private long getCountOfAppellantStatements(List<ScannedDocument> scannedDocuments, List<SscsDocument> sscsDocuments) {
        log.info("Counting statements");
        long statementCount = 0;
        statementCount = scannedDocuments.stream()
                .filter(doc -> doc.getValue() != null)
                .filter(doc -> StringUtils.isNotBlank(doc.getValue().getFileName()))
                .filter(doc -> doc.getValue().getFileName().startsWith(FILE_NAME_PREFIX)).count();

        log.info("Statement count 1: {}" + statementCount);

        for (SscsDocument sscsDocument: sscsDocuments) {
            log.info("sscsDocument.getValue().getDocumentFileName() {}", sscsDocument.getValue().getDocumentFileName());
        }

        if (sscsDocuments != null) {
            statementCount += sscsDocuments.stream()
                    .filter(sscsDoc -> sscsDoc.getValue() != null)
                    .filter(sscsDoc -> StringUtils.isNotBlank(sscsDoc.getValue().getDocumentFileName()))
                    .filter(sscsDoc -> sscsDoc.getValue().getDocumentFileName().startsWith(FILE_NAME_PREFIX)).count();
        }
        log.info("Statement count 2: {}" + statementCount);
        return statementCount;
    }

    @Override
    protected PdfAppellantStatement getPdfContent(AppellantStatementPdfData data, String onlineHearingId, PdfAppealDetails appealDetails) {
        return new PdfAppellantStatement(appealDetails, data.getStatement().getBody());
    }
}
