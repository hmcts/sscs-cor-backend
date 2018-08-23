package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.io.UnsupportedEncodingException;

public class CcdStub {

    private static final String caseDetailsJson = "[{\n" +
            "  \"id\": \"{caseId}\",\n" +
            "  \"case_data\": {\n" +
            "      \"caseReference\": \"{caseReference}\",\n" +
            "      \"appeal\": {\n" +
            "        \"appellant\": {\n" +
            "          \"name\": {\n" +
            "            \"firstName\": \"{firstName}\",\n" +
            "            \"lastName\": \"{lastName}\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "  }\n" +
            "}]";

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

    public void stubSearchCaseWithEmailAddress(String email, Long caseId, String caseReference, String firstName, String lastName) throws JsonProcessingException, UnsupportedEncodingException {
        String caseDetailsJson = createCaseDetails(caseId, caseReference, firstName, lastName);

        wireMock.stubFor(get(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases?" +
                        "case.subscriptions.appellantSubscription.email=" + encode(email, UTF_8.name())
                ))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(caseDetailsJson))
        );
    }

    private String createCaseDetails(Long caseId, String caseReference, String firstName, String lastName) {
        return caseDetailsJson.replace("{caseId}", caseId.toString())
                .replace("{caseReference}", caseReference)
                .replace("{firstName}", firstName)
                .replace("{lastName}", lastName);
    }
}
