package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

import java.util.Arrays;

public class PdfServiceStub extends BaseStub {
    public PdfServiceStub(String url) {
        super(url);
    }

    public void stubCreatePdf() {
        wireMock.stubFor(post("/pdfs")
                .willReturn(ok(Arrays.toString(new byte[]{1, 2, 3}))));
    }
}
