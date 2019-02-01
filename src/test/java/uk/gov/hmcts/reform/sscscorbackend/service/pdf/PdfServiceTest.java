package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Test;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.pdfservice.PdfService;

public class PdfServiceTest {
    @Test
    public void createsPdf() throws IOException {
        PDFServiceClient pdfServiceClient = mock(PDFServiceClient.class);
        I18nBuilder i18nBuilder = mock(I18nBuilder.class);
        HashMap i18n = new HashMap();
        when(i18nBuilder.build()).thenReturn(i18n);
        PdfService appellantTemplatePath = new PdfService(pdfServiceClient, "/templates/onlineHearingSummary.html", i18nBuilder);

        PdfSummary pdfSummary = DataFixtures.somePdfSummary();

        byte[] expectedPdf = new byte[]{ 1, 2, 3};
        when(pdfServiceClient.generateFromHtml(any(), eq(ImmutableMap.of("pdfSummary", pdfSummary, "i18n", i18n))))
                .thenReturn(expectedPdf);

        byte[] pdf = appellantTemplatePath.createPdf(pdfSummary);

        assertThat(pdf, is(expectedPdf));
    }
}