package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreQuestionsPdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;

@Service
public class QuestionRoundIssuedEventAction implements CohEventAction {
    private final StoreQuestionsPdfService storeQuestionsPdfService;
    private final CorEmailService corEmailService;
    private final DwpEmailMessageBuilder dwpEmailMessageBuilder;

    public QuestionRoundIssuedEventAction(StoreQuestionsPdfService storeQuestionsPdfService,
                                          CorEmailService corEmailService,
                                          DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        this.storeQuestionsPdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.dwpEmailMessageBuilder = dwpEmailMessageBuilder;
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
        corEmailService.sendPdfToDwp(
                cohEventActionContext,
                getDwpEmailSubject(sscsCaseDetails),
                getDwpEmailMessage(sscsCaseDetails)
        );
    }

    private String getDwpEmailMessage(SscsCaseDetails sscsCaseDetails) {
        return dwpEmailMessageBuilder.getQuestionMessage(sscsCaseDetails);
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
