package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.io.UnsupportedEncodingException;
import uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain.CaseDetails;

public class CcdStub {
    private final WireMockServer wireMock;

    public CcdStub(String url) {
        wireMock = new WireMockServer(Integer.valueOf(url.split(":")[2]));
        wireMock.start();
    }

    public void printAllRequests() {
        if (System.getenv("PRINT_REQUESTS") != null) {
            wireMock.findAll(RequestPatternBuilder.allRequests()).forEach(request -> {
                System.out.println("**********************CCD**********************");
                System.out.println(request);
                System.out.println("**********************CCD**********************");
            });
        }
    }

    public void shutdown() {
        wireMock.stop();
    }

    public void stubSearchCaseWithEmailAddress(String email, Long caseId) throws JsonProcessingException, UnsupportedEncodingException {
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();
        String caseDetailsJson = new ObjectMapper().writeValueAsString(singletonList(caseDetails));

        wireMock.stubFor(get(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases?" +
                        "case.subscriptions.appellantSubscription.email=" + encode(email, UTF_8.name())
                ))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(caseDetailsJson))
        );
    }
}
