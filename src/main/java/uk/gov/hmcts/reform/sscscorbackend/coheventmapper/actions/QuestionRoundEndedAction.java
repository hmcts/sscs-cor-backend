package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

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
    public CohEventActionContext handle(Long caseId, String onlineHearingId, SscsCaseDetails sscsCaseDetails) {
        CohEventActionContext actionContext = storePdfService.storePdf(caseId, onlineHearingId, new PdfData(sscsCaseDetails));

        String caseReference = sscsCaseDetails.getData().getCaseReference();
        corEmailService.sendFileToDwp(
                actionContext,
                getDwpEmailSubject(caseReference),
                emailMessageBuilder.getAnswerMessage(sscsCaseDetails)
        );

        return actionContext;
    }

    protected String getDwpEmailSubject(String caseReference) {
        return "Appellant has provided information (" + caseReference + ")";
    }
}
