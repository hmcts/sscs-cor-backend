package uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice;

public interface PdfService {
    byte[] createPdf(Object pdfSummary, String templatePath);
}
