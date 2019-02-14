package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

@FunctionalInterface
public interface CohEventAction {
    void handle(Long caseId, String onlineHearingId, CohEvent cohEvent);
}
