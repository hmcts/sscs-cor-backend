package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;

@Service
public class DeadlineExtensionDeniedEventAction implements CohEventAction {
    @Override
    public String cohEvent() {
        return "question_deadline_extension_denied";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_QUESTION_DEADLINE_EXTENSION_DENIED;
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}
