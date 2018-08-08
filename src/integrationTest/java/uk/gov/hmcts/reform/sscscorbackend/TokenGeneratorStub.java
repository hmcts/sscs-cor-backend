package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;

public class TokenGeneratorStub {
    private final WireMockServer wireMock;

    public TokenGeneratorStub(String url) {
        wireMock = new WireMockServer(Integer.valueOf(url.split(":")[2]));
        wireMock.start();

        wireMock.stubFor(post(urlEqualTo("/lease"))
                .willReturn(ok("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNTMzNjY5MTgyfQ.Tr6nlcxFptSp9qPcgImowv5yDivPeX32nLwumDAEJgAEt4U_RHYx1gUJyK7GqRe1o1eE-tCNbaNMW5OIbbENdg")
                        .withHeader("Content-Type", "text/plain;charset=UTF-8")));
    }

    public void shutdown() {
        wireMock.stop();
    }
}
