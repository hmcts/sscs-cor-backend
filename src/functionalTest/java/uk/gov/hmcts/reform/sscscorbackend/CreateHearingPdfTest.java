package uk.gov.hmcts.reform.sscscorbackend;

import java.io.IOException;

import org.junit.Ignore;
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

        // Dirty fix for now. As we create the hearing with COH on AAT it calls back to COR on AAT. Therefore it can be
        // adding events to the case as we call the decision issued. This can then fail in CCD with a
        // uk.gov.hmcts.ccd.endpoint.exceptions.CaseConcurrencyException. Need to think about a better way to handle
        // this.
        Thread.sleep(10000L);

        decisionIssued(onlineHearing.getHearingId(), onlineHearing.getCaseId());
    }
}
