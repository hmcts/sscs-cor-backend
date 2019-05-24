package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.evidence.EvidenceUploadEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAnswersPdfService;

@Service
public class AnswerSubmittedEventAction extends QuestionRoundEndedAction {

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, EmailMessageBuilder emailMessageBuilder, EvidenceUploadEmailService evidenceUploadEmailService, QuestionService questionService) {
        super(storeAnswersPdfService, corEmailService, emailMessageBuilder, evidenceUploadEmailService, questionService);
    }

    @Override
    protected boolean shouldHandleQuestionRound(QuestionRound questions) {
        return questions.getQuestions().stream()
                    .allMatch(questionSummary -> submitted.equals(questionSummary.getAnswerState()));
    }

    @Override
    public String cohEvent() {
        return "answers_submitted";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_ANSWERS_SUBMITTED;
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}
