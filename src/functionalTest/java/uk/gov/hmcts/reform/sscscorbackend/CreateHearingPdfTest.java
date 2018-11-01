package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class CreateHearingPdfTest extends BaseFunctionTest {

    @Test
    public void testCreate() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);

        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());

        //now trigger our endpoint
        resolveHearing(onlineHearing.getHearingId(), onlineHearing.getCaseId());
    }



}
