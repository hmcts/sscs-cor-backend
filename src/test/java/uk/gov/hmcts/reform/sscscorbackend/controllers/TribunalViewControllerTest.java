package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.TribunalViewResponse;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;

public class TribunalViewControllerTest {
    private String onlineHearingId;
    private TribunalViewController underTest;
    private OnlineHearingService onlineHearingService;

    @Before
    public void setUp() {
        onlineHearingId = "someHearingId";
        onlineHearingService = mock(OnlineHearingService.class);
        underTest = new TribunalViewController(onlineHearingService);
    }

    @Test
    public void recordTribunalViewResponse() {
        TribunalViewResponse tribunalViewResponse = new TribunalViewResponse("accepted", "Reasons");
        ResponseEntity response = underTest.recordTribunalViewResponse(onlineHearingId, tribunalViewResponse);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        verify(onlineHearingService).addDecisionReply(onlineHearingId, tribunalViewResponse);
    }
}
