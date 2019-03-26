package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class PdfData {
    private final SscsCaseDetails caseDetails;

    public PdfData(SscsCaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }

    public SscsCaseDetails getCaseDetails() {
        return caseDetails;
    }
}
