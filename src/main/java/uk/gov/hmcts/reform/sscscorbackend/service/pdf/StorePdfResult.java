package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class StorePdfResult {
    private final Pdf pdf;
    private final SscsCaseDetails document;

    public StorePdfResult(Pdf pdf, SscsCaseDetails document) {
        this.pdf = pdf;
        this.document = document;
    }

    public Pdf getPdf() {
        return pdf;
    }

    public SscsCaseDetails getDocument() {
        return document;
    }
}
