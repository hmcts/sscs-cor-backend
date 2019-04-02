package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;

public class AppellantStatementPdfData extends PdfData {

    private final Statement statement;

    public AppellantStatementPdfData(SscsCaseDetails caseDetails, Statement statement) {
        super(caseDetails);
        this.statement = statement;
    }

    public Statement getStatement() {
        return statement;
    }
}
