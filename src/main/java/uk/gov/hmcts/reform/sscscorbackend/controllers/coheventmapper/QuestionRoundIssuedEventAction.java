package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications.NotificationsService;

@Service
public class QuestionRoundIssuedEventAction implements CohEventAction {
    private final NotificationsService notificationsService;
    private final StoreQuestionsPdfService storeQuestionsPdfService;
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public QuestionRoundIssuedEventAction(NotificationsService notificationsService,
                                          StoreQuestionsPdfService storeQuestionsPdfService,
                                          CorEmailService corEmailService,
                                          DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.notificationsService = notificationsService;
        this.storeQuestionsPdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId, CohEvent cohEvent) {
        notificationsService.send(cohEvent);
        StorePdfResult storePdfResult = storeQuestionsPdfService.storePdf(
                caseId,
                onlineHearingId
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
