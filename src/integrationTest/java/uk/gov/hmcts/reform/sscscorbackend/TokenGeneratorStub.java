package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import uk.gov.hmcts.reform.sscscorbackend.service.idam.Authorize;
import uk.gov.hmcts.reform.sscscorbackend.service.idam.UserDetails;

// todo split this into s2s and idam stub
public class TokenGeneratorStub {
    private final WireMockServer wireMock;

    public TokenGeneratorStub(String url, String idamRedirectUrl, String clientId, String clientSecret) throws JsonProcessingException, UnsupportedEncodingException {
        wireMock = new WireMockServer(Integer.valueOf(url.split(":")[2]));
        wireMock.start();

        stubHealth();
        stubLease();
        stubGetIdamTokens(idamRedirectUrl, clientId, clientSecret);
    }

    public void printAllRequests() {
        if (System.getenv("PRINT_REQUESTS") != null) {
            wireMock.findAll(RequestPatternBuilder.allRequests()).forEach(request -> {
                System.out.println("**********************IDAM**********************");
                System.out.println(request);
                System.out.println("**********************IDAM**********************");
            });
        }
    }

    public void shutdown() {
        wireMock.stop();
    }

    private void stubHealth() {
        wireMock.stubFor(get(urlEqualTo("/health"))
                .willReturn(okJson("{\"status\": \"UP\"}")));
    }

    private void stubLease() {
        wireMock.stubFor(post(urlEqualTo("/lease"))
                .willReturn(ok("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNTMzNjY5MTgyfQ.Tr6nlcxFptSp9qPcgImowv5yDivPeX32nLwumDAEJgAEt4U_RHYx1gUJyK7GqRe1o1eE-tCNbaNMW5OIbbENdg")
                        .withHeader("Content-Type", "text/plain;charset=UTF-8")));
    }

    private void stubGetIdamTokens(String idamRedirectUrl, String clientId, String clientSecret) throws JsonProcessingException, UnsupportedEncodingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String authorizeCode = "someCode";
        Authorize authorize = Authorize.builder().code(authorizeCode).build();
        String authorizeJson = objectMapper.writeValueAsString(authorize);
        String redirectUrl = URLEncoder.encode(idamRedirectUrl, StandardCharsets.UTF_8.name());
        wireMock.stubFor(post(urlEqualTo("/oauth2/authorize?" +
                "response_type=code&" +
                "client_id=" + clientId + "&" +
                "redirect_uri=" + redirectUrl))
                .willReturn(okJson(authorizeJson))
        );

        Authorize accessToken = Authorize.builder().accessToken("someAccessToken").build();
        String accessTokenJson = objectMapper.writeValueAsString(accessToken);
        wireMock.stubFor(post(urlEqualTo("/oauth2/token?" +
                "code=" + authorizeCode + "&" +
                "grant_type=authorization_code&" +
                "redirect_uri=" + redirectUrl + "&" +
                "client_id=" + clientId + "&" +
                "client_secret=" + clientSecret))
                .willReturn(okJson(accessTokenJson))
        );

        String userDetailsJson = objectMapper.writeValueAsString(UserDetails.builder().id("someId").build());
        wireMock.stubFor(get(urlEqualTo("/details"))
                .willReturn(okJson(userDetailsJson))
        );
    }

    public static void main(String[] args) throws Exception {
        new TokenGeneratorStub("http://localhost:4502", "https://localhost:9000/poc", "clientId", "clientSecret");
    }
}
