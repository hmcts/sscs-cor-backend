package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static java.lang.Long.valueOf;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

public class CohEventActionRunnerTest {

    private NotificationsService notificationService;
    private CohEventAction cohEventAction;
    private String hearingId;
    private CohEvent cohEvent;
    private StorePdfService storePdfService;
    private StorePdfResult storePdfResult;
    private Long caseIdLong;
    private CohEventActionRunner cohEventActionRunner;

    @Before
    public void setUp() {
        notificationService = mock(NotificationsService.class);
        cohEventAction = mock(CohEventAction.class);
        String caseId = "123456";
        hearingId = "hearingId";
        cohEvent = someCohEvent(caseId, hearingId, "some-event");
        storePdfService = mock(StorePdfService.class);
        when(cohEventAction.getPdfService()).thenReturn(storePdfService);
        storePdfResult = mock(StorePdfResult.class);
        caseIdLong = valueOf(caseId);
        when(storePdfService.storePdf(caseIdLong, hearingId)).thenReturn(storePdfResult);
        cohEventActionRunner = new CohEventActionRunner(notificationService);
    }

    @Test
    public void syncStoresPdfHandlesActionAndSendNotificaiton() {
        when(cohEventAction.notifyAppellant()).thenReturn(true);

        cohEventActionRunner.runActionSync(cohEvent, cohEventAction);

        verify(storePdfService).storePdf(caseIdLong, hearingId);
        verify(cohEventAction).handle(caseIdLong, hearingId, storePdfResult);
        verify(notificationService).send(cohEvent);
    }

    @Test
    public void syncStoresPdfHandlesActionAndDoesNotSendNotificaiton() {
        when(cohEventAction.notifyAppellant()).thenReturn(false);

        cohEventActionRunner.runActionSync(cohEvent, cohEventAction);

        verify(storePdfService).storePdf(caseIdLong, hearingId);
        verify(cohEventAction).handle(caseIdLong, hearingId, storePdfResult);
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void asyncStoresPdfHandlesActionAndSendNotificaiton() {
        when(cohEventAction.notifyAppellant()).thenReturn(true);

        cohEventActionRunner.runActionAsync(cohEvent, cohEventAction);

        verify(storePdfService).storePdf(caseIdLong, hearingId);
        verify(cohEventAction).handle(caseIdLong, hearingId, storePdfResult);
        verify(notificationService).send(cohEvent);
    }

    @Test
    public void asyncStoresPdfHandlesActionAndDoesNotSendNotificaiton() {
        when(cohEventAction.notifyAppellant()).thenReturn(false);

        cohEventActionRunner.runActionAsync(cohEvent, cohEventAction);

        verify(storePdfService).storePdf(caseIdLong, hearingId);
        verify(cohEventAction).handle(caseIdLong, hearingId, storePdfResult);
        verifyZeroInteractions(notificationService);
    }
}