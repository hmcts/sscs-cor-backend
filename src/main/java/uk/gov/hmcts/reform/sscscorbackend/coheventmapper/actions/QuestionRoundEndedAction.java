package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.evidence.EvidenceUploadEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.PdfData;

public abstract class QuestionRoundEndedAction implements CohEventAction {
    protected final StorePdfService<?, PdfData> storePdfService;
    protected final CorEmailService corEmailService;
    protected final EmailMessageBuilder emailMessageBuilder;
    private final EvidenceUploadEmailService evidenceUploadEmailService;
    private final QuestionService questionService;

    public QuestionRoundEndedAction(StorePdfService<?, PdfData> storeQuestionsPdfService, CorEmailService corEmailService, EmailMessageBuilder emailMessageBuilder, EvidenceUploadEmailService evidenceUploadEmailService, QuestionService questionService) {
        this.storePdfService = storeQuestionsPdfService;
        this.corEmailService = corEmailService;
        this.emailMessageBuilder = emailMessageBuilder;
        this.evidenceUploadEmailService = evidenceUploadEmailService;
        this.questionService = questionService;
    }

    @Override
    public CohEventActionContext handle(Long caseId, String onlineHearingId, SscsCaseDetails sscsCaseDetails) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId, true);

        if (shouldHandleQuestionRound(questions)) {
            CohEventActionContext actionContext = storePdfService.storePdf(caseId, onlineHearingId,
                new PdfData(sscsCaseDetails));

            String caseReference = sscsCaseDetails.getData().getCaseReference();
            corEmailService.sendFileToDwp(
                    actionContext,
                    getDwpEmailSubject(caseReference),
                    emailMessageBuilder.getAnswerMessage(sscsCaseDetails)
            );

            evidenceUploadEmailService.sendQuestionEvidenceToDwp(questions.getQuestions(), sscsCaseDetails);

            return actionContext;
        }
        return new CohEventActionContext(null, sscsCaseDetails);
    }

    abstract boolean shouldHandleQuestionRound(QuestionRound questions);


    protected String getDwpEmailSubject(String caseReference) {
        return "Appellant has provided information (" + caseReference + ")";
    }
}
