package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;
import org.junit.Test;

public class CreateHearingPdfTest extends BaseFunctionTest {

    @Test
    public void testCreate() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);

        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());

        //now trigger our endpoint
        relistHearing(onlineHearing.getHearingId(), onlineHearing.getCaseId());
    }

    @Test
    public void recordRejectedResponse() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);
        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());
        createAndIssueDecision(onlineHearing.getHearingId(), onlineHearing.getCaseId());

        decisionIssued(onlineHearing.getHearingId(), onlineHearing.getCaseId());
    }
}
