package uk.gov.hmcts.reform.sscscorbackend.service;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = CohClient.class)
public class CohClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Autowired
    public CohClient cohClient;

    private String json = "{\n" +
            "  \"current_question_state\": {\n" +
            "    \"state_datetime\": \"string\",\n" +
            "    \"state_name\": \"string\"\n" +
            "  },\n" +
            "  \"deadline_expiry_date\": \"string\",\n" +
            "  \"owner_reference\": \"string\",\n" +
            "  \"question_body_text\": \"string\",\n" +
            "  \"question_header_text\": \"string\",\n" +
            "  \"question_id\": \"string\",\n" +
            "  \"question_ordinal\": \"string\",\n" +
            "  \"question_round\": \"string\"\n" +
            "}";

    @Test
    public void testGetQuestion() {
        stubFor(get(urlEqualTo("/continuous-online-hearings/1/questions/1"))
                .willReturn(aResponse().withBody(json)));

        Question question = cohClient.getQuestion("1", "1");

        assertNotNull(question);
    }

}
