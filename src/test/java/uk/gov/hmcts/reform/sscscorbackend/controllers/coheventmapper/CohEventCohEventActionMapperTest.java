package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

public class CohEventCohEventActionMapperTest {

    private CohEventActionMapper cohEventActionMapper;
    private CohEventAction action;
    private NotificationsService notificationService;
    private String caseId;
    private long caseIdLong;
    private String hearingId;
    private StorePdfResult storePdfResult;
    private StorePdfService storePdfService;

    @Before
    public void setUp() {
        caseId = "1234";
        caseIdLong = Long.valueOf(caseId);
        hearingId = "hearingId";
        action = mock(CohEventAction.class);
        when(action.eventCanHandle()).thenReturn("someMappedEvent");
        storePdfService = mock(StorePdfService.class);
        storePdfResult = mock(StorePdfResult.class);
        when(storePdfService.storePdf(caseIdLong, hearingId)).thenReturn(storePdfResult);
        when(action.getPdfService()).thenReturn(storePdfService);
        List<CohEventAction> actions = singletonList(action);
        notificationService = mock(NotificationsService.class);
        cohEventActionMapper = new CohEventActionMapper(actions, notificationService);
    }

    @Test
    public void handlesEventAndShouldSendNotification() {
        when(action.notifyAppellant()).thenReturn(true);
        CohEvent cohEvent = someCohEvent(caseId, hearingId, "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(action).handle(caseIdLong, hearingId, storePdfResult);
        verify(storePdfService).storePdf(caseIdLong, hearingId);
        verify(notificationService).send(cohEvent);
        assertThat(handle, is(true));
    }

    @Test
    public void handlesEventAndShouldNotSendNotification() {
        when(action.notifyAppellant()).thenReturn(false);
        CohEvent cohEvent = someCohEvent(caseId, hearingId, "someMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(action).handle(caseIdLong, hearingId, storePdfResult);
        verify(storePdfService).storePdf(caseIdLong, hearingId);
        verifyZeroInteractions(notificationService);
        assertThat(handle, is(true));
    }

    @Test
    public void cannotHandleEventCallsNoActions() {
        CohEvent cohEvent = someCohEvent(caseId, hearingId, "someUnMappedEvent");
        boolean handle = cohEventActionMapper.handle(cohEvent);

        verify(action, never()).handle(any(Long.class), any(String.class), any(StorePdfResult.class));
        verifyZeroInteractions(notificationService);
        assertThat(handle, is(false));
    }
}