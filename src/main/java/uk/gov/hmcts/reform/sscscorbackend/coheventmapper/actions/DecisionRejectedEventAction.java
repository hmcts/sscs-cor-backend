package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;

@Service
public class DecisionRejectedEventAction implements CohEventAction {
    @Override
    public String cohEvent() {
        return "decision_rejected";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_DECISION_REJECTED;
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}
