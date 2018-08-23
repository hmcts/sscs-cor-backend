package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import uk.gov.hmcts.reform.sscscorbackend.service.idam.Authorize;
import uk.gov.hmcts.reform.sscscorbackend.service.idam.UserDetails;

public class IdamStub extends BaseStub {

    public IdamStub(String url, String idamRedirectUrl, String clientId, String clientSecret) throws JsonProcessingException, UnsupportedEncodingException {
        super(url);

        stubGetIdamTokens(idamRedirectUrl, clientId, clientSecret);
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

}
