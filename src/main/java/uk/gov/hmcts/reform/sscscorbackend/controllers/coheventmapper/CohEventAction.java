package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

public interface CohEventAction {
    void handle(Long caseId, String onlineHearingId);

    String eventCanHandle();

    default boolean notifyAppellant() {
        return true;
    }
}
