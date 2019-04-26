package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.email.AppellantStatementEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.AppellantStatementPdfData;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;

@Service
public class AppellantStatementService {
    private final StoreAppellantStatementService storeAppellantStatementService;
    private final OnlineHearingService onlineHearingService;
    private final AppellantStatementEmailService appellantStatementEmailService;

    @Autowired
    public AppellantStatementService(
            StoreAppellantStatementService storeAppellantStatementService,
            OnlineHearingService onlineHearingService,
            AppellantStatementEmailService appellantStatementEmailService) {
        this.storeAppellantStatementService = storeAppellantStatementService;
        this.onlineHearingService = onlineHearingService;
        this.appellantStatementEmailService = appellantStatementEmailService;
    }

    public Optional<CohEventActionContext> handleAppellantStatement(String onlineHearingId, Statement statement) {
        return onlineHearingService.getCcdCase(onlineHearingId).map(caseDetails -> {
            CohEventActionContext cohEventActionContext = storeAppellantStatementService.storePdf(
                    caseDetails.getId(),
                    onlineHearingId,
                    new AppellantStatementPdfData(caseDetails, statement)
            );
            appellantStatementEmailService.sendEmail(cohEventActionContext);

            return cohEventActionContext;
        });
    }
}
