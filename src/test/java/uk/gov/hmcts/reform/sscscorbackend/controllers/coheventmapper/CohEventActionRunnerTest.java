package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import static java.lang.Long.valueOf;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

public class CohEventActionRunnerTest {

    private NotificationsService notificationService;
    private CohEventAction cohEventAction;
    private String hearingId;
    private CohEvent cohEvent;
    private CohEventActionContext cohEventActionContext;
    private Long caseIdLong;
    private CohEventActionRunner cohEventActionRunner;
    private CorCcdService corCcdService;
    private IdamTokens idamTokens;
    private SscsCaseData sscsCaseData;
    private EventType ccdEvent;
    private SscsCaseDetails sscsCaseDetails;

    @Before
    public void setUp() {
        notificationService = mock(NotificationsService.class);
        cohEventAction = mock(CohEventAction.class);
        String caseId = "123456";
        hearingId = "hearingId";
        cohEvent = someCohEvent(caseId, hearingId, "some-event");

        cohEventActionContext = mock(CohEventActionContext.class);
        caseIdLong = valueOf(caseId);

        CohEventActionContext cohEventActionContextHandle = mock(CohEventActionContext.class);
        sscsCaseData = SscsCaseData.builder().build();
        sscsCaseDetails = SscsCaseDetails.builder()
                .data(sscsCaseData)
                .build();
        when(cohEventActionContextHandle.getDocument()).thenReturn(sscsCaseDetails);
        when(cohEventAction.createAndStorePdf(caseIdLong, hearingId, sscsCaseDetails)).thenReturn(cohEventActionContext);
        when(cohEventAction.handle(caseIdLong, hearingId, cohEventActionContext)).thenReturn(cohEventActionContextHandle);
        ccdEvent = EventType.COH_ANSWERS_SUBMITTED;
        when(cohEventAction.getCcdEventType()).thenReturn(ccdEvent);

        corCcdService = mock(CorCcdService.class);
        IdamService idamService = mock(IdamService.class);
        idamTokens = mock(IdamTokens.class);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        when(corCcdService.getByCaseId(caseIdLong, idamTokens)).thenReturn(this.sscsCaseDetails);

        cohEventActionRunner = new CohEventActionRunner(corCcdService, idamService, notificationService);
    }

    @Test
    public void syncStoresPdfHandlesActionAndSendNotificaiton() {
        when(cohEventAction.notifyAppellant()).thenReturn(true);

        cohEventActionRunner.runActionSync(cohEvent, cohEventAction);

        verify(cohEventAction).createAndStorePdf(caseIdLong, hearingId, sscsCaseDetails);
        verify(cohEventAction).handle(caseIdLong, hearingId, cohEventActionContext);
        verify(notificationService).send(cohEvent);
        verify(corCcdService).updateCase(sscsCaseData,
                caseIdLong,
                ccdEvent.getCcdType(),
                "SSCS COH - Event received",
                "Coh event [" + cohEventAction.cohEvent() + "] received",
                idamTokens);
    }

    @Test
    public void syncStoresPdfHandlesActionAndDoesNotSendNotification() {
        when(cohEventAction.notifyAppellant()).thenReturn(false);

        cohEventActionRunner.runActionSync(cohEvent, cohEventAction);

        verify(cohEventAction).createAndStorePdf(caseIdLong, hearingId, sscsCaseDetails);
        verify(cohEventAction).handle(caseIdLong, hearingId, cohEventActionContext);
        verifyZeroInteractions(notificationService);
        verify(corCcdService).updateCase(sscsCaseData,
                caseIdLong,
                ccdEvent.getCcdType(),
                "SSCS COH - Event received",
                "Coh event [" + cohEventAction.cohEvent() + "] received",
                idamTokens);
    }

    @Test
    public void asyncStoresPdfHandlesActionAndSendNotification() {
        when(cohEventAction.notifyAppellant()).thenReturn(true);

        cohEventActionRunner.runActionAsync(cohEvent, cohEventAction);

        verify(cohEventAction).createAndStorePdf(caseIdLong, hearingId, sscsCaseDetails);
        verify(cohEventAction).handle(caseIdLong, hearingId, cohEventActionContext);
        verify(notificationService).send(cohEvent);
        verify(corCcdService).updateCase(sscsCaseData,
                caseIdLong,
                ccdEvent.getCcdType(),
                "SSCS COH - Event received",
                "Coh event [" + cohEventAction.cohEvent() + "] received",
                idamTokens);
    }

    @Test
    public void asyncStoresPdfHandlesActionAndDoesNotSendNotification() {
        when(cohEventAction.notifyAppellant()).thenReturn(false);

        cohEventActionRunner.runActionAsync(cohEvent, cohEventAction);

        verify(cohEventAction).createAndStorePdf(caseIdLong, hearingId, sscsCaseDetails);
        verify(cohEventAction).handle(caseIdLong, hearingId, cohEventActionContext);
        verifyZeroInteractions(notificationService);
        verify(corCcdService).updateCase(sscsCaseData,
                caseIdLong,
                ccdEvent.getCcdType(),
                "SSCS COH - Event received",
                "Coh event [" + cohEventAction.cohEvent() + "] received",
                idamTokens);
    }
}