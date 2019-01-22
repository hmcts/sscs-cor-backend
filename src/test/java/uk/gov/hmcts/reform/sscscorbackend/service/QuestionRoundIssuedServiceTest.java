package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.someCohEvent;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

public class QuestionRoundIssuedServiceTest {


    private NotificationsService notificationsService;
    private StoreQuestionsPdfService storeQuestionsPdfService;
    private CorEmailService corEmailService;
    private QuestionRoundIssuedService questionRoundIssuedService;
    private Long caseId;
    private String hearingId;

    @Before
    public void setUp() {
        notificationsService = mock(NotificationsService.class);
        storeQuestionsPdfService = mock(StoreQuestionsPdfService.class);
        corEmailService = mock(CorEmailService.class);
        questionRoundIssuedService = new QuestionRoundIssuedService(
                notificationsService, storeQuestionsPdfService, corEmailService
        );
        caseId = 123456L;
        hearingId = "someHearingId";
    }

    @Test
    public void sendQuestionsToDwp() {
        StorePdfResult storePdfResult = mock(StorePdfResult.class);
        when(storeQuestionsPdfService.storePdf(caseId, hearingId)).thenReturn(storePdfResult);
        CohEvent cohEvent = someCohEvent(caseId.toString(), hearingId, "some_event");

        questionRoundIssuedService.handleQuestionRoundIssued(cohEvent);

        verify(notificationsService).send(cohEvent);
        verify(corEmailService).sendPdf(storePdfResult);
    }
}