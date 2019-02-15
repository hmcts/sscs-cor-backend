package uk.gov.hmcts.reform.sscscorbackend.controllers.coheventmapper;

import uk.gov.hmcts.reform.sscscorbackend.service.BasePdfService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StorePdfResult;

public interface CohEventAction {
    void handle(Long caseId, String onlineHearingId, StorePdfResult storePdfResult);

    String eventCanHandle();

    BasePdfService getPdfService();

    default boolean notifyAppellant() {
        return true;
    }
}
