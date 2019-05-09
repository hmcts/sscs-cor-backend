package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

@Service
public class QuestionRoundIssuedEventAction implements CohEventAction {
    private final StoreQuestionsPdfService storeQuestionsPdfService;
    private final CorEmailService corEmailService;
    private final EmailMessageBuilder emailMessageBuilder;

    public QuestionRoundIssuedEventAction(StoreQuestionsPdfService storeQuestionsPdfService,
                                          CorEmailService corEmailService,
                                          EmailMessageBuilder emailMessageBuilder) {
        this.storeQuestionsPdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
    }

    @Override
    public CohEventActionContext createAndStorePdf(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        return storeQuestionsPdfService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails));
    }

    @Override
    public CohEventActionContext handle(Long caseId, String onlineHearingId, CohEventActionContext cohEventActionContext) {
        SscsCaseDetails sscsCaseDetails = cohEventActionContext.getDocument();
        sendDwpEmail(cohEventActionContext, sscsCaseDetails);

        return cohEventActionContext;
    }

    public StorePdfService getPdfService() {
        return storeQuestionsPdfService;
    }

    private void sendDwpEmail(CohEventActionContext cohEventActionContext, SscsCaseDetails sscsCaseDetails) {
        corEmailService.sendFileToDwp(
                cohEventActionContext,
                getDwpEmailSubject(sscsCaseDetails),
                getDwpEmailMessage(sscsCaseDetails)
        );
    }

    private String getDwpEmailMessage(SscsCaseDetails sscsCaseDetails) {
        return emailMessageBuilder.getQuestionMessage(sscsCaseDetails);
    }

    private String getDwpEmailSubject(SscsCaseDetails sscsCaseDetails) {
        return "Questions issued to the appellant (" + sscsCaseDetails.getData().getCaseReference() + ")";
    }

    @Override
    public String cohEvent() {
        return "question_round_issued";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_QUESTION_ROUND_ISSUED;
    }
}
