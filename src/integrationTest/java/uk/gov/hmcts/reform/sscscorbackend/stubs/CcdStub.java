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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

public class CcdStub extends BaseStub {

    private static final String caseDetailsJson = "{\n" +
            "  \"id\": \"{caseId}\",\n" +
            "  \"case_data\": {\n" +
            "      \"caseReference\": \"{caseReference}\",\n" +
            "      \"onlinePanel\": {\n" +
            "        \"assignedTo\": \"someJudge\"\n" +
            "      },\n" +
            "      \"assignedToJudge\": \"someJudge\"," +
            "      \"assignedToDisabilityMember\": \"someDisabilityMember\"," +
            "      \"assignedToMedicalMember\": \"someMedicalMember\"," +
            "      \"appeal\": {\n" +
            "         \"mrnDetails\": {\n" +
            "             \"dwpIssuingOffice\": \"1\",\n" +
            "             \"mrnDate\": \"2018-02-01\"\n" +
            "         }," +
            "        \"hearingType\": \"cor\",\n" +
            "        \"appellant\": {\n" +
            "          \"name\": {\n" +
            "            \"firstName\": \"{firstName}\",\n" +
            "            \"lastName\": \"{lastName}\"\n" +
            "          },\n" +
            "           \"address\": {\n" +
            "            \"line1\": \"123 Hairy Lane\",\n" +
            "            \"line2\": \"Off Hairy Park\",\n" +
            "            \"town\": \"Town\",\n" +
            "            \"county\": \"County\",\n" +
            "            \"postcode\": \"TS3 3ST\"\n" +
            "          },\n" +
            "          \"contact\": {\n" +
            "            \"email\": \"harry.potter@wizards.com\",\n" +
            "            \"mobile\": \"07411999999\"\n" +
            "          }," +
            "          \"identity\": { \"nino\": \"nino\" }\n" +
            "        },\n" +
            "        \"benefitType\": { \"code\": \"PIP\" }\n" +
            "      },\n" +
            "      \"draftCorDocument\": [\n" +
            "        {\n" +
            "          \"value\": {\n" +
            "            \"document\": {\n" +
            "              \"documentFileName\": \"{evidenceFileName}\",\n" +
            "              \"documentDateAdded\": \"{evidenceCreatedDate}\",\n" +
            "              \"documentLink\": {\n" +
            "                \"document_url\": \"{evidenceUrl}\",\n" +
            "                \"document_binary_url\": \"{evidenceUrl}\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"questionId\": \"{evidenceQuestionId}\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]," +
            "      \"draftSscsDocument\": [\n" +
            "        {\n" +
            "          \"value\": {\n" +
            "              \"documentFileName\": \"{evidenceFileName}\",\n" +
            "              \"documentDateAdded\": \"{evidenceCreatedDate}\",\n" +
            "              \"documentLink\": {\n" +
            "                \"document_url\": \"{evidenceUrl}\",\n" +
            "                \"document_binary_url\": \"{evidenceUrl}\"\n" +
            "              }\n" +
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

    public void stubFindCaseByCaseId(Long caseId, SscsCaseData.SscsCaseDataBuilder sscsCaseDetails) throws JsonProcessingException {
        wireMock.stubFor(get(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId))
                .withHeader("ServiceAuthorization", new RegexPattern(".*"))
                .willReturn(okJson(new ObjectMapper().writeValueAsString(SscsCaseDetails.builder()
                        .id(caseId)
                        .data(sscsCaseDetails.build())
                        .build())))
        );
    }

    public void stubUpdateCase(Long caseId, String caseReference) throws JsonProcessingException {
        wireMock.stubFor(get("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/event-triggers/uploadDocument/token")
                .willReturn(okJson(new ObjectMapper().writeValueAsString(StartEventResponse.builder().build()))));

        wireMock.stubFor(post("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true")
                .willReturn(okJson(createCaseDetails(caseId, caseReference, "firstName", "lastName", "evidenceQuestionId", "evidenceFileName", "evidenceCreatedDate", "http://localhost:4603/documents/123"))));
    }

    public void stubUpdateCaseWithEvent(Long caseId, final String eventType, String caseReference) throws JsonProcessingException {
        wireMock.stubFor(get("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/event-triggers/" + eventType + "/token")
                .willReturn(okJson(new ObjectMapper().writeValueAsString(StartEventResponse.builder().build()))));

        wireMock.stubFor(post("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true")
                .willReturn(okJson(createCaseDetails(caseId, caseReference, "firstName", "lastName",
                    "evidenceQuestionId",
                    "temporal unique Id ec7ae162-9834-46b7-826d-fdc9935e3187 evidenceFileName",
                    "evidenceCreatedDate", "http://localhost:4603/documents/123"))));
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

    public void verifyUpdateCaseWithPdfToScannedDocuments(Long caseId, String caseReference, String pdfName) {
        verifyAsync(postRequestedFor(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true"))
                .withRequestBody(matchingJsonPath("$.event.summary", equalTo("SSCS - upload document event")))
                .withRequestBody(matchingJsonPath("$.data.caseReference", equalTo(caseReference)))
                .withRequestBody(matchingJsonPath("$.data.scannedDocuments[0].value.fileName", equalTo(pdfName)))
        );
    }

    public void verifyUpdateCaseToOralHearing(Long caseId, String caseReference) {
        verifyAsync(postRequestedFor(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/events?ignore-warning=true"))
                .withRequestBody(matchingJsonPath("$.event.summary", equalTo("SSCS - appeal updated event")))
                .withRequestBody(matchingJsonPath("$.data.caseReference", equalTo(caseReference)))
                .withRequestBody(matchingJsonPath("$.data.appeal.hearingType", equalTo("oral")))
        );
    }

    public void stubRemovePanelMember(Long caseId, String memberToRemove) {
        wireMock.stubFor(delete("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/users/" + memberToRemove)
            .willReturn(ok()));
    }

    public void verifyRemovePanelMember(Long caseId, String memberToRemove) {
        verifyAsync(deleteRequestedFor(urlEqualTo("/caseworkers/someId/jurisdictions/SSCS/case-types/Benefit/cases/" + caseId + "/users/" + memberToRemove)));
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

    public static SscsCaseData.SscsCaseDataBuilder baseCaseData(String caseReference) {
        return SscsCaseData.builder()
                .caseReference(caseReference)
                .appeal(Appeal.builder()
                        .hearingType("cor")
                        .appellant(Appellant.builder()
                                .name(Name.builder()
                                        .firstName("firstName")
                                        .lastName("lastName")
                                        .build())
                                .identity(Identity.builder()
                                        .nino("nino")
                                        .build())
                                .address(Address.builder()
                                        .line1("line1")
                                        .line2("line2")
                                        .town("town")
                                        .county("county")
                                        .postcode("postcode")
                                        .build())
                                .contact(Contact.builder()
                                        .email("email")
                                        .phone("012")
                                        .mobile("120")
                                        .build())
                                .build())
                        .benefitType(BenefitType.builder().code("PIP").build())
                        .build());
    }
}
