package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.Statement;
import uk.gov.hmcts.reform.sscscorbackend.service.email.AppellantStatementEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAppellantStatementService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.AppellantStatementPdfData;

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

    public Optional<CohEventActionContext> handleAppellantStatement(String identifier, Statement statement) {
        return onlineHearingService.getCcdCaseByIdentifier(identifier).map(caseDetails -> {
            CohEventActionContext cohEventActionContext = storeAppellantStatementService.storePdf(
                    caseDetails.getId(),
                    identifier,
                    new AppellantStatementPdfData(caseDetails, statement)
            );
            appellantStatementEmailService.sendEmail(cohEventActionContext);

            return cohEventActionContext;
        });
    }
}
