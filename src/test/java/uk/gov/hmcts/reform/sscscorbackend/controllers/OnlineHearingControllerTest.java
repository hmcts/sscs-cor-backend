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
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

public class OnlineHearingControllerTest {
    private OnlineHearingService onlineHearingService;
    private OnlineHearingController onlineHearingController;
    private CohEventActionMapper cohEventActionMapper;

    @Before
    public void setUp() {
        onlineHearingService = mock(OnlineHearingService.class);
        cohEventActionMapper = mock(CohEventActionMapper.class);
        onlineHearingController = new OnlineHearingController(
                onlineHearingService,
                cohEventActionMapper);
    }

    @Test
    public void testCatchEvent() {
        String caseId = "caseId";

        CcdEvent ccdEvent = someCcdEvent(caseId);

        when(onlineHearingService.createOnlineHearing(ccdEvent)).thenReturn(true);

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

        Long caseId = 12345L;

        CohEvent cohEvent = someCohEvent(caseId.toString(), hearingId, "continuous_online_hearing_resolved");
        when(cohEventActionMapper.handle(cohEvent)).thenReturn(true);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(stringResponseEntity.getBody(), is(""));
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
    public void testCatchCohEventUnhandled() {
        String hearingId = null;

        String caseId = "caseId";

        CohEvent cohEvent = someCohEvent(caseId, hearingId, "continuous_online_hearing_started");
        when(cohEventActionMapper.handle(cohEvent)).thenReturn(false);

        ResponseEntity<String> stringResponseEntity = onlineHearingController.catchCohEvent(cohEvent);

        assertThat(stringResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }
}
