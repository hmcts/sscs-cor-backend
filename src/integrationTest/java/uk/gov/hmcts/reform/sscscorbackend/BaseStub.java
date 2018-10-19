package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

public abstract class BaseStub {
    protected final WireMockServer wireMock;

    public BaseStub(String url) {
        wireMock = new WireMockServer(Integer.valueOf(url.split(":")[2]));
        wireMock.start();

        stubHealth();
    }

    public void printAllRequests() {
        if (System.getenv("PRINT_REQUESTS") != null) {
            wireMock.findAll(RequestPatternBuilder.allRequests()).forEach(request -> {
                String stubName = this.getClass().getSimpleName().replace("Stub", "");
                System.out.println("**********************" + stubName + "**********************");
                System.out.println(request);
                System.out.println("**********************" + stubName + "**********************");
            });
        }
    }

    public void shutdown() {
        printAllRequests();
        wireMock.stop();
    }

    protected void stubHealth() {
        wireMock.stubFor(get(urlEqualTo("/health"))
                .willReturn(okJson("{\"status\": \"UP\"}")));
    }
}
