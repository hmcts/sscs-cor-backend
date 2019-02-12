package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class QuestionRoundIssuedService {
    private final NotificationsService notificationsService;
    private final StoreQuestionsPdfService storeQuestionsPdfService;
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public QuestionRoundIssuedService(NotificationsService notificationsService,
                                      StoreQuestionsPdfService storeQuestionsPdfService,
                                      CorEmailService corEmailService,
                                      DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.notificationsService = notificationsService;
        this.storeQuestionsPdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    public void handleQuestionRoundIssued(CohEvent cohEvent) {
        notificationsService.send(cohEvent);
        StorePdfResult storePdfResult = storeQuestionsPdfService.storePdf(
                Long.valueOf(cohEvent.getCaseId()),
                cohEvent.getOnlineHearingId()
        );
        SscsCaseDetails sscsCaseDetails = storePdfResult.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendPdf(
                storePdfResult,
                "Questions issued to the appellant (" + caseReference + ")",
                dwpEmailMessageBuilder.getQuestionMessage(sscsCaseDetails)
        );
    }
}
