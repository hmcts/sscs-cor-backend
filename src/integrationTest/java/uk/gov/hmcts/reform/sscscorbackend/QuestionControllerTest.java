package uk.gov.hmcts.reform.sscscorbackend;

import io.restassured.RestAssured;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuestionControllerTest {

    private static final String QUESTION_HEADER = "Question Header";

    private CohStub cohStub;
    private TokenGeneratorStub tokenGeneratorStub;

    @Value("${coh.url}")
    private String cohUrl;
    @Value("${idam.s2s-auth.url}")
    private String tokenGeneratorUrl;
    @LocalServerPort
    private int applicationPort;

    @Before
    public void setUp() {
        cohStub = new CohStub(cohUrl);
        tokenGeneratorStub = new TokenGeneratorStub(tokenGeneratorUrl);
    }

    @After
    public void shutdownCoh() {
        if (cohStub != null) {
            cohStub.shutdown();
        }
        if (tokenGeneratorStub != null) {
            tokenGeneratorStub.shutdown();
        }
    }

    @Test
    public void getSampleQuestion() {
        cohStub.stubGetQuestion("1", "1", QUESTION_HEADER);

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/1/questions/1")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(new QuestionHeaderMatcher(QUESTION_HEADER));
    }

    @Test
    public void get404WhenQuestionDoesNotExist() {
        cohStub.stubCannotFindQuestion("2", "2");

        RestAssured.baseURI = "http://localhost:" + applicationPort;
        RestAssured.given()
                .when()
                .get("/continuous-online-hearings/2/questions/2")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
