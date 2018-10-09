package uk.gov.hmcts.reform.sscscorbackend;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import java.io.UnsupportedEncodingException;

public class CcdStub extends BaseStub {

    private static final String caseDetailsJson = "[{\n" +
            "  \"id\": \"{caseId}\",\n" +
            "  \"case_data\": {\n" +
            "      \"caseReference\": \"{caseReference}\",\n" +
            "      \"onlinePanel\": {\n" +
            "        \"assignedTo\": \"someJudge\"\n" +
            "      },\n" +
            "      \"appeal\": {\n" +
            "        \"hearingType\": \"cor\",\n" +
            "        \"appellant\": {\n" +
            "          \"name\": {\n" +
            "            \"firstName\": \"{firstName}\",\n" +
            "            \"lastName\": \"{lastName}\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "  }\n" +
            "}]";

    public CcdStub(String url) {
        super(url);
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
