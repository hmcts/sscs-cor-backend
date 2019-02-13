package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.*;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

@Service
public class AnswerSubmittedEventAction implements CohEventAction {
    private final CorEmailService corEmailService;
    private final BasePdfService storeAnswersPdfService;
    private final AnswersEmailMessageBuilder answersEmailMessageBuilder;

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, AnswersEmailMessageBuilder answersEmailMessageBuilder) {
        this.corEmailService = corEmailService;
        this.storeAnswersPdfService = storeAnswersPdfService;
        this.answersEmailMessageBuilder = answersEmailMessageBuilder;
    }

    @Override
    public void handle(Long caseId, String onlineHearingId, CohEvent cohEvent) {
        StorePdfResult storePdfResult = storeAnswersPdfService.storePdf(caseId, onlineHearingId);
        SscsCaseDetails sscsCaseDetails = storePdfResult.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendPdf(
                storePdfResult,
                "Appellant has provided information (" + caseReference + ")",
                answersEmailMessageBuilder.getMessage(sscsCaseDetails)
        );
    }
}
