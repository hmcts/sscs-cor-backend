package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class QuestionRoundIssuedService {
    private final NotificationsService notificationsService;
    private final StoreQuestionsPdfService storeQuestionsPdfService;
    private final CorEmailService corEmailService;

    public QuestionRoundIssuedService(NotificationsService notificationsService, StoreQuestionsPdfService storeQuestionsPdfService, CorEmailService corEmailService) {
        this.notificationsService = notificationsService;
        this.storeQuestionsPdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
    }

    public void handleQuestionRoundIssued(CohEvent cohEvent) {
        notificationsService.send(cohEvent);
        StorePdfResult storePdfResult = storeQuestionsPdfService.storePdf(
                Long.valueOf(cohEvent.getCaseId()),
                cohEvent.getOnlineHearingId()
        );
        corEmailService.sendPdf(storePdfResult);
    }
}
