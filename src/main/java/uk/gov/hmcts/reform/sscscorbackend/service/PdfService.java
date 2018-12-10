package uk.gov.hmcts.reform.sscscorbackend.service;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sscs.exception.PdfGenerationException;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;

@Service
public class PdfService {
    private final PDFServiceClient pdfServiceClient;
    private final String appellantTemplatePath;

    public PdfService(@Autowired PDFServiceClient pdfServiceClient,
                      @Value("${online_hearing_finished.html.template.path}") String appellantTemplatePath) {
        this.pdfServiceClient = pdfServiceClient;
        this.appellantTemplatePath = appellantTemplatePath;
    }

    public byte[] createPdf(PdfSummary pdfSummary) {
        Map<String, Object> placeholders = ImmutableMap.of("online_hearing_pdf_wrapper", pdfSummary);

        try {
            byte[] template = getTemplate();
            return pdfServiceClient.generateFromHtml(template, placeholders);
        } catch (IOException e) {
            throw new PdfGenerationException("Error getting template " + appellantTemplatePath, e);
        }
    }

    private byte[] getTemplate() throws IOException {
        InputStream in = getClass().getResourceAsStream(appellantTemplatePath);
        return IOUtils.toByteArray(in);
    }
}
