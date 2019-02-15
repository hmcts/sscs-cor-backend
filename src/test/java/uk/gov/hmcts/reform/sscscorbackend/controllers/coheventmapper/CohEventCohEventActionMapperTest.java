package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

public class CohEventCohEventActionMapperTest {

    private CohEventActionMapper cohEventActionMapper;
    private CohEventAction action;
    private NotificationsService notificationService;

    @Before
    public void setUp() {
        HashMap<String, CohEventAction> actions = new HashMap<>();
        action = mock(CohEventAction.class);
        actions.put("someMappedEvent", action);
        notificationService = mock(NotificationsService.class);
        cohEventActionMapper = new CohEventActionMapper(actions, notificationService);
    }

    @Test
    public void handlesEventAndShouldSendNotification() {
        when(action.notifyAppellant()).thenReturn(true);
        CohEvent cohEvent = someCohEvent("1234", "hearingId", "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(action).handle(1234L, "hearingId");
        verify(notificationService).send(cohEvent);
        assertThat(handle, is(true));
    }

    @Test
    public void handlesEventAndShouldNotSendNotification() {
        when(action.notifyAppellant()).thenReturn(false);
        CohEvent cohEvent = someCohEvent("1234", "hearingId", "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(action).handle(1234L, "hearingId");
        verifyZeroInteractions(notificationService);
        assertThat(handle, is(true));
    }

    @Test
    public void cannotHandleEventCallsNoActions() {
        CohEvent cohEvent = someCohEvent("1234", "hearingId", "someUnMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verifyZeroInteractions(action);
        assertThat(handle, is(false));
    }
}