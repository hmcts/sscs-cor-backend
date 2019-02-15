package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

public interface CohEventAction {
    void handle(Long caseId, String onlineHearingId);

    default boolean notifyAppellant() {
        return true;
    }
}
