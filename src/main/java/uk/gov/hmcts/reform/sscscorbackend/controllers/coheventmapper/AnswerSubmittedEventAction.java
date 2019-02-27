package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersPdfService;

@Service
public class AnswerSubmittedEventAction extends QuestionRoundEndedAction {

    public AnswerSubmittedEventAction(CorEmailService corEmailService, StoreAnswersPdfService storeAnswersPdfService, DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        super(storeAnswersPdfService, corEmailService, dwpEmailMessageBuilder);
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
