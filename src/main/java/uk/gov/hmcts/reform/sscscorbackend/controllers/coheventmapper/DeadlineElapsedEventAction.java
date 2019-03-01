package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.service.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.DwpEmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.StoreAnswersDeadlineElapsedPdfService;

@Service
public class DeadlineElapsedEventAction extends QuestionRoundEndedAction {

    @Autowired
    public DeadlineElapsedEventAction(
            CorEmailService corEmailService,
            StoreAnswersDeadlineElapsedPdfService storeQuestionsPdfService,
            DwpEmailMessageBuilder dwpEmailMessageBuilder) {
        super(storeQuestionsPdfService, corEmailService, dwpEmailMessageBuilder);
    }

    @Override
    protected String getDwpEmailSubject(String caseReference) {
        return "Appellant has provided information (" + caseReference + ")";
    }

    @Override
    public String cohEvent() {
        return "question_deadline_elapsed";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_QUESTION_DEADLINE_ELAPSED;
    }
}