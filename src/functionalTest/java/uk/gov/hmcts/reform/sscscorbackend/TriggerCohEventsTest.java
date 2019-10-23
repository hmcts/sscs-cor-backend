package uk.gov.hmcts.reform.sscscorbackend;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

import java.io.IOException;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class TriggerCohEventsTest extends BaseFunctionTest {

    @Test
    public void relistingHearingUpdatesCcd() throws IOException, InterruptedException {
        OnlineHearing onlineHearing = createHearingWithQuestion(true);

        answerQuestion(onlineHearing.getHearingId(), onlineHearing.getQuestionId());

        String relistingReason = "some relisting reason";
        cohRequests.setRelistingReason(onlineHearing.getHearingId(), relistingReason);

        SscsCaseDetails caseDetails = getCaseDetails(onlineHearing.getCaseId());

        assertThat(caseDetails.getData().getRelistingReason(), is(nullValue()));
        assertThat(caseDetails.getData().getAppeal().getHearingType(), is("cor"));

        //now trigger our endpoint
        relistHearing(onlineHearing.getHearingId(), onlineHearing.getCaseId());
        waitForCcdEvent(onlineHearing.getCaseId(), EventType.COH_ONLINE_HEARING_RELISTED);

        caseDetails = getCaseDetails(onlineHearing.getCaseId());

        assertThat(caseDetails.getData().getRelistingReason(), is(relistingReason));
        assertThat(caseDetails.getData().getAppeal().getHearingType(), is("oral"));
    }
}
