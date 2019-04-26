package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.Pdf;

public class CohEventActionContext {
    private final Pdf pdf;
    private final SscsCaseDetails document;

    public CohEventActionContext(Pdf pdf, SscsCaseDetails document) {
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
