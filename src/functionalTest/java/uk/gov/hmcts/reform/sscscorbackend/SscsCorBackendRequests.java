package uk.gov.hmcts.reform.sscscorbackend;

import static org.apache.http.client.methods.RequestBuilder.*;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Slf4j
public class SscsCorBackendRequests {
    private final IdamTokens idamTokens;
    private final CorIdamService corIdamService;
    private String baseUrl;
    private CloseableHttpClient client;


    public SscsCorBackendRequests(IdamService idamService, CorIdamService corIdamService, String baseUrl, CloseableHttpClient client) {
        this.idamTokens = idamService.getIdamTokens();
        this.corIdamService = corIdamService;
        this.baseUrl = baseUrl;
        this.client = client;
    }

    public JSONArray getOnlineHearingForCitizen(String tya, String email) throws IOException {
        String uri = (StringUtils.isNotBlank(tya)) ? "/citizen/" + tya : "/citizen";
        HttpResponse getOnlineHearingResponse = getRequest(uri, email);

        assertThat(getOnlineHearingResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(getOnlineHearingResponse.getEntity());

        return new JSONArray(responseBody);
    }

    public JSONObject assignCaseToUser(String tya, String email, String postcode) throws IOException {
        StringEntity entity = new StringEntity("{\"email\":\"" + email + "\", \"postcode\":\"" + postcode + "\"}", APPLICATION_JSON);

        HttpResponse response = postRequest("/citizen/" + tya, entity, email);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(response.getEntity());

        return new JSONObject(responseBody);
    }

    public CreatedCcdCase createOralCase(String emailAddress) throws IOException {
        HttpResponse createCaseResponse = client.execute(post(baseUrl + "/case?hearingType=oral&email=" + emailAddress)
                .setHeader("Content-Length", "0")
                .build());

        assertThat(createCaseResponse.getStatusLine().getStatusCode(), is(HttpStatus.CREATED.value()));

        String responseBody = EntityUtils.toString(createCaseResponse.getEntity());
        JSONObject jsonObject = new JSONObject(responseBody);
        System.out.println("Case id " + jsonObject.getString("id"));
        return new CreatedCcdCase(
                jsonObject.getString("id"),
                jsonObject.getString("appellant_tya")
        );
    }

    public void uploadHearingEvidence(String hearingId, String fileName) throws IOException {
        HttpEntity data = MultipartEntityBuilder.create()
            .setContentType(ContentType.MULTIPART_FORM_DATA)
            .addBinaryBody("file",
                Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(fileName)),
                ContentType.IMAGE_PNG,
                fileName)
            .build();

        HttpResponse response = putRequest("/continuous-online-hearings/" + hearingId + "/evidence", data);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
    }

    public void submitHearingEvidence(String hearingId, String description) throws IOException {
        HttpResponse response = postRequest("/continuous-online-hearings/" + hearingId + "/evidence",
            new StringEntity("{\n"
                + "  \"body\": \"" + description + "\",\n"
                + "  \"idamEmail\": \"mya-sscs-6920@mailinator.com\"\n"
                + "}", APPLICATION_JSON)
        );
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public JSONArray getDraftHearingEvidence(String hearingId) throws IOException {
        HttpResponse response = getRequest("/continuous-online-hearings/" + hearingId + "/evidence");
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));

        String responseBody = EntityUtils.toString(response.getEntity());

        return new JSONArray(responseBody);
    }

    public void uploadAppellantStatement(String hearingId, String statement) throws IOException {
        String uri = "/continuous-online-hearings/" + hearingId + "/statement";
        String stringEntity = "{\n"
            + "  \"body\": \"statement\",\n"
            + "  \"tya\": \"Q9jE2FQuRR\"\n"
            + "}";
        HttpResponse getQuestionResponse = postRequest(uri, new StringEntity(stringEntity, APPLICATION_JSON));

        assertThat(getQuestionResponse.getStatusLine().getStatusCode(), is(HttpStatus.NO_CONTENT.value()));
    }

    public String getCoversheet(String caseId) throws IOException {
        CloseableHttpResponse getCoverSheetResponse = getRequest("/continuous-online-hearings/" + caseId + "/evidence/coversheet");

        assertThat(getCoverSheetResponse.getStatusLine().getStatusCode(), is(HttpStatus.OK.value()));
        Header fileNameHeader = getCoverSheetResponse.getFirstHeader("Content-Disposition");
        return ContentDisposition.parse(fileNameHeader.getValue()).getFilename();
    }

    private RequestBuilder addHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
                .setHeader(HttpHeaders.AUTHORIZATION, idamTokens.getIdamOauth2Token())
                .setHeader("ServiceAuthorization", idamTokens.getServiceAuthorization());
    }

    private RequestBuilder addHeaders(RequestBuilder requestBuilder, String email) {
        String userToken = corIdamService.getUserToken(email, "Apassword123");
        return requestBuilder
                .setHeader(HttpHeaders.AUTHORIZATION, userToken)
                .setHeader("ServiceAuthorization", idamTokens.getServiceAuthorization());
    }

    private CloseableHttpResponse getRequest(String url) throws IOException {
        return client.execute(addHeaders(get(baseUrl + url))
                .build());
    }

    private CloseableHttpResponse getRequest(String url, String email) throws IOException {
        return client.execute(addHeaders(get(baseUrl + url), email)
                .build());
    }

    private CloseableHttpResponse putRequest(String url, HttpEntity body) throws IOException {
        return client.execute(addHeaders(put(baseUrl + url))
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse postRequestNoBody(String url) throws IOException {
        return client.execute(addHeaders(post(baseUrl + url)
                .setHeader("Content-Length", "0"))
                .build());
    }

    private CloseableHttpResponse postRequest(String url, HttpEntity body) throws IOException {
        return client.execute(addHeaders(post(baseUrl + url))
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse postRequest(String url, HttpEntity body, String email) throws IOException {
        return client.execute(addHeaders(post(baseUrl + url), email)
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse patchRequestNoBody(String url) throws IOException {
        return client.execute(addHeaders(patch(baseUrl + url))
                .build());
    }

    private CloseableHttpResponse patchRequest(String url, StringEntity body) throws IOException {
        return client.execute(addHeaders(patch(baseUrl + url))
                .setEntity(body)
                .build());
    }

    private CloseableHttpResponse deleteRequest(String url) throws IOException {
        return client.execute(addHeaders(delete(baseUrl + url))
                .build());
    }
}
