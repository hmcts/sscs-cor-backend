package uk.gov.hmcts.reform.sscscorbackend.service;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.util.IOUtils;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

public class PdfService {
    private final PDFServiceClient pdfServiceClient;
    private final byte[] template;
    private final HashMap i18n;

    public PdfService(PDFServiceClient pdfServiceClient, String appellantTemplatePath, I18nBuilder i18nBuilder) throws IOException {
        this.pdfServiceClient = pdfServiceClient;

        template = getResource(appellantTemplatePath);
        i18n = i18nBuilder.build();
    }

    public byte[] createPdf(Object pdfSummary) {
        Map<String, Object> placeholders = ImmutableMap.of("pdfSummary", pdfSummary, "i18n", i18n);

        return pdfServiceClient.generateFromHtml(template, placeholders);
    }

    private byte[] getResource(String file) throws IOException {
        InputStream in = getClass().getResourceAsStream(file);
        return IOUtils.toByteArray(in);
    }
}
