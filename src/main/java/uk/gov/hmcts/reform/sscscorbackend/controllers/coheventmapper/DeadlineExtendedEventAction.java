package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

@Service
public class DeadlineExtendedEventAction implements CohEventAction {
    @Override
    public String cohEvent() {
        return "question_deadline_extended";
    }

    @Override
    public CohEventActionContext handle(Long caseId, String onlineHearingId, CohEventActionContext cohEventActionContext) {
        return cohEventActionContext;
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_QUESTION_DEADLINE_EXTENDED;
    }

    @Override
    public boolean notifyAppellant() {
        return false;
    }
}