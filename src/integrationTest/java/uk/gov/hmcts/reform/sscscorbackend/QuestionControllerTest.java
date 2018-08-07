package uk.gov.hmcts.reform.sscscorbackend;

import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class QuestionControllerTest {

    private static final String QUESTION_HEADER = "Question Header";
    private CohStub cohStub;

    @Value("${server.port}")
    public int applicationPort;
    @Value("${coh.url}")
    public String cohUrl;

    @Before
    public void setUp() {
        cohStub = new CohStub(cohUrl);
    }

    @After
    public void shutdownCoh() {
        if (cohStub != null) {
            cohStub.shutdown();
        }
    }

    @Test
    public void getSampleQuestion() {
        cohStub.stubGetQuestion("1", "1", QUESTION_HEADER);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured
                .given()
                .when()
                .get("/continuous-online-hearings/1/questions/1")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(new QuestionHeaderMatcher(QUESTION_HEADER));
    }
}
