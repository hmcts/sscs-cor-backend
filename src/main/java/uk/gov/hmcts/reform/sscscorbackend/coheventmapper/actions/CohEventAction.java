package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

public interface CohEventAction {
    String cohEvent();

    default CohEventActionContext handle(Long caseId, String onlineHearingId, SscsCaseDetails caseDetails) {
        return new CohEventActionContext(null, caseDetails);
    }

    EventType getCcdEventType();

    default boolean notifyAppellant() {
        return true;
    }
}
