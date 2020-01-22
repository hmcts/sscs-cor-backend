package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.ScannedDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.service.CcdPdfService;
import uk.gov.hmcts.reform.sscs.service.EvidenceManagementService;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppellantStatement;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

@Service
public class StoreAppellantStatementService extends StorePdfService<PdfAppellantStatement, AppellantStatementPdfData> {

    private static final String APPELLANT_STATEMENT = "Appellant statement ";
    private static final String REPRESENTATIVE_STATEMENT = "Representative statement ";

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
    protected String documentNamePrefix(SscsCaseDetails caseDetails, String onlineHearingId,
                                        AppellantStatementPdfData data) {
        return workOutIfAppellantOrRepsStatement(caseDetails, data)
            + getCountOfNextStatement(caseDetails.getData().getScannedDocuments()) + " - ";
    }

    @NotNull
    private String workOutIfAppellantOrRepsStatement(SscsCaseDetails caseDetails, AppellantStatementPdfData data) {
        Subscription repsSubs = caseDetails.getData().getSubscriptions().getRepresentativeSubscription();
        String statementPrefix = APPELLANT_STATEMENT;
        if (repsSubs != null && data.getStatement().getTya().equals(repsSubs.getTya())) {
            statementPrefix = REPRESENTATIVE_STATEMENT;
        }
        return statementPrefix;
    }

    private long getCountOfNextStatement(List<ScannedDocument> scannedDocuments) {
        if (scannedDocuments == null) {
            return 1;
        }
        return scannedDocuments.stream()
            .filter(doc -> doc.getValue() != null)
            .filter(doc -> StringUtils.isNotBlank(doc.getValue().getFileName()))
            .filter(this::docFileNameIsStatement).count() + 1;
    }

    private boolean docFileNameIsStatement(ScannedDocument doc) {
        return doc.getValue().getFileName().startsWith(APPELLANT_STATEMENT)
            || doc.getValue().getFileName().startsWith(REPRESENTATIVE_STATEMENT);
    }

    @Override
    protected PdfAppellantStatement getPdfContent(AppellantStatementPdfData data, String onlineHearingId,
                                                  PdfAppealDetails appealDetails) {
        return new PdfAppellantStatement(appealDetails, data.getStatement().getBody());
    }
}
