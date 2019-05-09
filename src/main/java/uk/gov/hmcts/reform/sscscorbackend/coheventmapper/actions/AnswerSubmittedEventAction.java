package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.submitted;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAnswersPdfService;

@Service
public class AnswerSubmittedEventAction extends QuestionRoundEndedAction {

    private final QuestionService questionService;

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, EmailMessageBuilder emailMessageBuilder, QuestionService questionService) {
        super(storeAnswersPdfService, corEmailService, emailMessageBuilder);
        this.questionService = questionService;
    }

    public CohEventActionContext handle(Long caseId, String onlineHearingId, SscsCaseDetails sscsCaseDetails) {
        QuestionRound questions = questionService.getQuestions(onlineHearingId, true);

        boolean questionRoundAnswered = questions.getQuestions().stream()
                .allMatch(questionSummary -> submitted.equals(questionSummary.getAnswerState()));

        if (questionRoundAnswered) {
            return super.handle(caseId, onlineHearingId, sscsCaseDetails);
        }
        return new CohEventActionContext(null, sscsCaseDetails);
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
