package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.PdfData;

public abstract class QuestionRoundEndedAction implements CohEventAction {
    protected final StorePdfService<?, PdfData> storePdfService;
    protected final CorEmailService corEmailService;
    protected final EmailMessageBuilder emailMessageBuilder;

    public QuestionRoundEndedAction(StorePdfService<?, PdfData> storeQuestionsPdfService, CorEmailService corEmailService, EmailMessageBuilder emailMessageBuilder) {
        this.storePdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
    }

    @Override
    public CohEventActionContext createAndStorePdf(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        return storePdfService.storePdf(caseId, onlineHearingId, new PdfData(caseDetails));
    }

    @Override
    public CohEventActionContext handle(Long caseId, String onlineHearingId, CohEventActionContext cohEventActionContext) {
        SscsCaseDetails sscsCaseDetails = cohEventActionContext.getDocument();
        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendPdfToDwp(
                cohEventActionContext,
                getDwpEmailSubject(caseReference),
                emailMessageBuilder.getAnswerMessage(sscsCaseDetails)
        );

        return cohEventActionContext;
    }

    protected abstract String getDwpEmailSubject(String caseReference);
}
