package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCcdEvent;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;



public class OnlineHearingControllerTest {
    private OnlineHearingService onlineHearingService;

    @Before
    public void setUp() {
        onlineHearingService = mock(OnlineHearingService.class);
    }

    @Test
    public void testCatchEvent() {
        String hearingId = "somehearingid";

        String caseId = "caseId";

        CcdEvent ccdEvent = someCcdEvent(caseId);

        when(onlineHearingService.createOnlineHearing(caseId)).thenReturn(hearingId);

        OnlineHearingController onlineHearingController = new OnlineHearingController(onlineHearingService);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchEvent(ccdEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(stringResponseEntity.getBody(), is(hearingId));
    }

    @Test
    public void testNullCaseId() {
        String hearingId = "somehearingid";

        String caseId = null;

        CcdEvent ccdEvent = someCcdEvent(caseId);

        when(onlineHearingService.createOnlineHearing(caseId)).thenReturn(hearingId);

        OnlineHearingController onlineHearingController = new OnlineHearingController(onlineHearingService);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchEvent(ccdEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
