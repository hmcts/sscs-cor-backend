package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class TokenGeneratorStub extends BaseStub {

    public TokenGeneratorStub(String url) {
        super(url);

        stubHealth();
        stubLease();
        stubServiceAuthDetails();
    }

    private void stubLease() {
        wireMock.stubFor(post(urlEqualTo("/lease"))
                .willReturn(ok("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNTMzNjY5MTgyfQ.Tr6nlcxFptSp9qPcgImowv5yDivPeX32nLwumDAEJgAEt4U_RHYx1gUJyK7GqRe1o1eE-tCNbaNMW5OIbbENdg")
                        .withHeader("Content-Type", "text/plain;charset=UTF-8")));
    }

    private void stubServiceAuthDetails() {
        wireMock.stubFor(
                get(urlEqualTo("/details")).withHeader("Authorization", equalTo("Bearer someServiceAuthHeader")).willReturn(
                        ok("sscs").withHeader("Content-Type", "text/plain;charset=UTF-8")
                )
        );
    }

    public static void main(String[] args) {
        new TokenGeneratorStub("http://localhost:4502");
    }
}
