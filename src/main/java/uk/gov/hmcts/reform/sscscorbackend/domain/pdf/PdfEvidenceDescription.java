package uk.gov.hmcts.reform.sscscorbackend.domain.pdf;

import java.util.List;

public class PdfEvidenceDescription {
    private final PdfAppealDetails appealDetails;
    private final String description;
    private final List<String> fileNames;

    public PdfEvidenceDescription(PdfAppealDetails appealDetails, String description, List<String> fileNames) {
        this.appealDetails = appealDetails;
        this.description = description;
        this.fileNames = fileNames;
    }

    public PdfAppealDetails getAppealDetails() {
        return appealDetails;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getFileNames() {
        return fileNames;
    }
}
