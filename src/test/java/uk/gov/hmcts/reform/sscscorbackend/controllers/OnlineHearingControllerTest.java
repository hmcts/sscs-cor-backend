package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCcdEvent;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreOnlineHearingTribunalsViewService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;


public class OnlineHearingControllerTest {
    private OnlineHearingService onlineHearingService;
    private StoreOnlineHearingService storeOnlineHearingService;
    private StoreOnlineHearingTribunalsViewService storeOnlineHearingTribunalsViewService;
    private OnlineHearingController onlineHearingController;
    private NotificationsService notificationsService;

    @Before
    public void setUp() {
        onlineHearingService = mock(OnlineHearingService.class);
        storeOnlineHearingService = mock(StoreOnlineHearingService.class);
        storeOnlineHearingTribunalsViewService = mock(StoreOnlineHearingTribunalsViewService.class);
        notificationsService = mock(NotificationsService.class);

        onlineHearingController = new OnlineHearingController(
                onlineHearingService,
                storeOnlineHearingService,
                storeOnlineHearingTribunalsViewService,
                notificationsService);
    }

    @Test
    public void testCatchEvent() {
        String caseId = "caseId";

        CcdEvent ccdEvent = someCcdEvent(caseId);

        when(onlineHearingService.createOnlineHearing(caseId)).thenReturn(true);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchEvent(ccdEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testNullCaseId() {
        String hearingId = "somehearingid";

        String caseId = null;

        CcdEvent ccdEvent = someCcdEvent(caseId);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchEvent(ccdEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testCatchCohEvent() {
        String hearingId = "somehearingid";

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_resolved");

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(stringResponseEntity.getBody(), is(""));
        verify(storeOnlineHearingService).storeOnlineHearingInCcd(hearingId, caseId);
    }

    @Test
    public void testCatchCohEventNullCaseId() {
        String hearingId = "somehearingid";

        String caseId = null;

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_resolved");

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void testCatchCohEventNullEvent() {
        String hearingId = null;

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, null);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void testCatchCohEventWrongEvent() {
        String hearingId = null;

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_started");

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testCatchDecisionIssuedCohEvent() {
        String hearingId = "somehearingid";

        String caseId = "1234";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "decision_issued");

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(stringResponseEntity.getBody(), is(""));
        verify(storeOnlineHearingTribunalsViewService).storeTribunalsView(Long.valueOf(caseId));
        verify(notificationsService).send(cohEvent);
    }
}
