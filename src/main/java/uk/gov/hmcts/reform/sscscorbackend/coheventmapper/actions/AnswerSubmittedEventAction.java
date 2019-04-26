package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;

@Service
public class AnswerSubmittedEventAction extends QuestionRoundEndedAction {

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, EmailMessageBuilder emailMessageBuilder) {
        super(storeAnswersPdfService, corEmailService, emailMessageBuilder);
    }

    protected String getDwpEmailSubject(String caseReference) {
        return "Appellant has provided information (" + caseReference + ")";
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
