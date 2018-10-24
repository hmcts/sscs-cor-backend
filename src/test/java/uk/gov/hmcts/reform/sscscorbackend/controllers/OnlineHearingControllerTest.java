package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCcdEvent;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.CohEvent;
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

    @Test
    public void testCatchCohEvent() {
        String hearingId = "somehearingid";

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_resolved");

        OnlineHearingController onlineHearingController = new OnlineHearingController(onlineHearingService);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(stringResponseEntity.getBody(), is(""));
    }


    @Test
    public void testCatchCohEventNullCaseId() {
        String hearingId = "somehearingid";

        String caseId = null;

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_resolved");

        OnlineHearingController onlineHearingController = new OnlineHearingController(onlineHearingService);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void testCatchCohEventNullEvent() {
        String hearingId = null;

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, null);

        OnlineHearingController onlineHearingController = new OnlineHearingController(onlineHearingService);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void testCatchCohEventWrongEvent() {
        String hearingId = null;

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_started");

        OnlineHearingController onlineHearingController = new OnlineHearingController(onlineHearingService);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

}
