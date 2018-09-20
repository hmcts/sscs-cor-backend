package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ExtendQuestionRoundDeadlineTest extends BaseFunctionTest {
    @Test
    public void extendsTheQuestionRoundDeadlineDate() throws IOException, InterruptedException {
        String hearingId = cohRequests.createHearing();
        cohRequests.createQuestion(hearingId);
        cohRequests.issueQuestionRound(hearingId);

        JSONObject questionRound = sscsCorBackendRequests.extendQuestionRoundDeadline(hearingId);
        String deadlineExpiryDate = questionRound.getString("deadline_expiry_date");

        assertThat(deadlineExpiryDate, is(notNullValue()));
    }
}
