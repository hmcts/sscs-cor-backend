package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;

public class CcdStub extends BaseStub {

    private static final String caseDetailsJson = "{\n" +
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
            "          },\n" +
            "          \"identity\": { \"nino\": \"nino\" }\n" +
            "        },\n" +
            "        \"benefitType\": { \"code\": \"PIP\" }\n" +
            "      },\n" +
            "      \"corDocument\": [\n" +
            "        {\n" +
            "          \"value\": {\n" +
            "            \"document\": {\n" +
            "              \"documentFileName\": \"{evidenceFileName}\",\n" +
            "              \"documentDateAdded\": \"{evidenceCreatedDate}\",\n" +
            "              \"documentLink\": {\n" +
            "                \"document_url\": \"{evidenceUrl}\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"questionId\": \"{evidenceQuestionId}\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "  }\n" +
            "}";

    public CcdStub(String url) {
        super(url);
    }

    public void stubSearchCaseWithEmailAddress(String email, Long caseId, String caseReference, String firstName, String lastName) throws JsonProcessingException, UnsupportedEncodingException {
        String caseDetailsJson = "[" + createCaseDetails(caseId, caseReference, firstName, lastName, "someQuestionId", "someFileName.txt", "2018-10-24'T'12:11:21Z", "http://example.com/document/1") + "]";

        wireMock.stubFor(get(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases?" +
                        "case.subscriptions.appellantSubscription.email=" + encode(email, UTF_8.name())
                ))
                        .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                        .willReturn(okJson(caseDetailsJson))
        );
    }

    public void stubFindCaseByCaseId(Long caseId, String evidenceQuestionId, String evidenceFileName, String evidenceCreatedDate, String evidenceUrl) {
        stubFindCaseByCaseId(caseId, "caseRef", evidenceQuestionId, evidenceFileName, evidenceCreatedDate, evidenceUrl);
    }

    public void stubFindCaseByCaseId(Long caseId, String caseReference, String evidenceQuestionId, String evidenceFileName, String evidenceCreatedDate, String evidenceUrl) {
        wireMock.stubFor(get(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(createCaseDetails(caseId, caseReference, "firstName", "lastName", evidenceQuestionId, evidenceFileName, evidenceCreatedDate, evidenceUrl)))
        );
    }

    public void stubUpdateCase(Long caseId) throws JsonProcessingException {
        wireMock.stubFor(get("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/event-triggers/uploadDocument/token")
                .willReturn(okJson(new ObjectMapper().writeValueAsString(StartEventResponse.builder().build()))));

        wireMock.stubFor(post("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true")
                .willReturn(okJson(new ObjectMapper().writeValueAsString(CaseDetails.builder().build()))));
    }

    public void stubUpdateCaseWithEvent(Long caseId, final String eventType) throws JsonProcessingException {
        wireMock.stubFor(get("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/event-triggers/" + eventType + "/token")
                .willReturn(okJson(new ObjectMapper().writeValueAsString(StartEventResponse.builder().build()))));

        wireMock.stubFor(post("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true")
                .willReturn(okJson(new ObjectMapper().writeValueAsString(CaseDetails.builder().build()))));
    }

    public void verifyUpdateCaseWithEvent(Long caseId, String eventType) {
        verifyAsync(getRequestedFor(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/event-triggers/" + eventType + "/token")));
    }

    public void verifyUpdateCaseWithPdf(Long caseId, String caseReference, String pdfName) {
        verifyAsync(postRequestedFor(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true"))
                .withRequestBody(matchingJsonPath("$.event.summary", equalTo("SSCS - upload document event")))
                .withRequestBody(matchingJsonPath("$.data.caseReference", equalTo(caseReference)))
                .withRequestBody(matchingJsonPath("$.data.sscsDocument[0].value.documentFileName", equalTo(pdfName)))
        );
    }

    public void verifyUpdateCaseToOralHearing(Long caseId, String caseReference) throws JsonProcessingException {
        verifyAsync(postRequestedFor(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true"))
                .withRequestBody(matchingJsonPath("$.event.summary", equalTo("SSCS - appeal updated event")))
                .withRequestBody(matchingJsonPath("$.data.caseReference", equalTo(caseReference)))
                .withRequestBody(matchingJsonPath("$.data.appeal.hearingType", equalTo("oral")))
        );
    }

    public void stubAddUserToCase(long caseId, String userToAdd) throws JsonProcessingException {
        wireMock.stubFor(post("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/users")
                .withRequestBody(equalToJson("{ \"id\": \"" + userToAdd + "\" }"))
                .willReturn(created()));
    }

    public void stubRemoveUserFromCase(long caseId, String userToRemove) throws JsonProcessingException {
        wireMock.stubFor(delete("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/users/" + userToRemove)
                .willReturn(created()));
    }

    public void stubGetHistoryEvents(Long caseId, EventType... eventTypes) {
        String responseJson = Arrays.stream(eventTypes)
                .map(event -> "{\"id\":\"" + event.getCcdType() + "\"}")
                .collect(Collectors.joining(",", "[", "]"));
        wireMock.stubFor(get("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events")
                .willReturn(okJson(responseJson))
        );
    }

    private String createCaseDetails(Long caseId, String caseReference, String firstName, String lastName, String evidenceQuestionId, String evidenceFileName, String evidenceCreatedDate, String evidenceUrl) {
        return caseDetailsJson.replace("{caseId}", caseId.toString())
                .replace("{caseReference}", caseReference)
                .replace("{firstName}", firstName)
                .replace("{lastName}", lastName)
                .replace("{evidenceQuestionId}", evidenceQuestionId)
                .replace("{evidenceFileName}", evidenceFileName)
                .replace("{evidenceCreatedDate}", evidenceCreatedDate)
                .replace("{evidenceUrl}", evidenceUrl);

    }
}
