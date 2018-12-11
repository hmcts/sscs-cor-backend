package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;

public class PdfServiceTest {
    @Test
    public void createsPdf() {
        PDFServiceClient pdfServiceClient = mock(PDFServiceClient.class);
        PdfService appellantTemplatePath = new PdfService(pdfServiceClient, "/templates/onlineHearingSummary.html");

        PdfSummary pdfSummary = DataFixtures.somePdfSummary();

        byte[] expectedPdf = new byte[]{ 1, 2, 3};
        when(pdfServiceClient.generateFromHtml(any(), eq(ImmutableMap.of("pdfSummary", pdfSummary))))
                .thenReturn(expectedPdf);

        byte[] pdf = appellantTemplatePath.createPdf(pdfSummary);

        assertThat(pdf, is(expectedPdf));
    }
}