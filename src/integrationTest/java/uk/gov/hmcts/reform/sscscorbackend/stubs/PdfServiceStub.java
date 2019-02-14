package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.poi.util.IOUtils;

public class PdfServiceStub extends BaseStub {
    public PdfServiceStub(String url) {
        super(url);
    }

    public void stubCreatePdf(byte[] pdf) {
        wireMock.stubFor(post("/pdfs")
                .willReturn(ok(Arrays.toString(pdf))));
    }

    public void verifyCreateDecisionIssuedPdf(String caseReference) throws IOException {
        verifyCreatePdf(caseReference, "/templates/tribunalsView.html", "$.values.pdfSummary.case_reference");
    }

    public void verifyCreateQuestionRoundIssuedPdf(String caseReference) throws IOException {
        verifyCreatePdf(caseReference, "/templates/questions.html", "$.values.pdfSummary.appealDetails.caseReference");
    }

    public void verifyCreateAnswersPdf(String caseReference) throws IOException {
        verifyCreatePdf(caseReference, "/templates/answers.html", "$.values.pdfSummary.appealDetails.caseReference");
    }

    public void verifySummaryPdf(String caseReference) throws IOException {
        verifyCreatePdf(caseReference, "/templates/onlineHearingSummary.html", "$.values.pdfSummary.appealDetails.caseReference");
    }

    public void verifyCreatePdf(String caseReference, String pdfTemplate, String caseReferencePath) throws IOException {
        InputStream in = getClass().getResourceAsStream(pdfTemplate);
        byte[] templateBytes = IOUtils.toByteArray(in);

        wireMock.verify(postRequestedFor(urlEqualTo("/pdfs"))
                .withRequestBody(matchingJsonPath(caseReferencePath, equalTo(caseReference)))
                .withRequestBody(matchingJsonPath("$.template", equalTo(new String(templateBytes))))
        );
    }
}
