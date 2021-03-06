package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;

@Service
public class DeadlineReminderEventAction implements CohEventAction {
    @Override
    public String cohEvent() {
        return "question_deadline_reminder";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_QUESTION_DEADLINE_REMINDER;
    }
}
