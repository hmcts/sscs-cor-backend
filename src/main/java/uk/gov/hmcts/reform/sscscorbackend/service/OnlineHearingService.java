package uk.gov.hmcts.reform.sscscorbackend.service;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohDecision;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohOnlineHearings;
import uk.gov.hmcts.reform.sscscorbackend.domain.Decision;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;

@Service
public class OnlineHearingService {
    private final CohService cohClient;
    private final CcdService ccdService;
    private final IdamService idamService;

    public OnlineHearingService(@Autowired CohService cohService,
                                @Autowired CcdService ccdService,
                                @Autowired IdamService idamService
    ) {
        this.cohClient = cohService;
        this.ccdService = ccdService;
        this.idamService = idamService;
    }

    public String createOnlineHearing(String caseId) {
        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId);

        //assume need to create it
        return cohClient.createOnlineHearing(createOnlineHearingRequest);
    }

    public Optional<OnlineHearing> getOnlineHearing(String emailAddress) {
        List<SscsCaseDetails> cases = ccdService.findCaseBy(
                ImmutableMap.of("case.subscriptions.appellantSubscription.email", emailAddress),
                idamService.getIdamTokens()
        );

        return cases.stream()
                .filter(caseDetails -> caseDetails.getData().getOnlinePanel() != null)
                .reduce(checkThereIsOnlyOneCase())
                .flatMap(getHearingFromCoh());
    }

    private Decision getDecision(String onlineHearingId) {
        Optional<CohDecision> decision = cohClient.getDecision(onlineHearingId);
        return decision.map(d -> new Decision(onlineHearingId, d.getDecisionAward(),
                    d.getDecisionHeader(), d.getDecisionReason(),
                    d.getDecisionText(), d.getCurrentDecisionState().getStateName()))
                .orElse(null);
    }

    private BinaryOperator<SscsCaseDetails> checkThereIsOnlyOneCase() {
        return (a, b) -> {
            throw new IllegalStateException("Multiple appeals with online hearings found.");
        };
    }

    private Function<SscsCaseDetails, Optional<OnlineHearing>> getHearingFromCoh() {
        return firstCase -> {
            CohOnlineHearings cohOnlineHearings = cohClient.getOnlineHearing(firstCase.getId());

            return cohOnlineHearings.getOnlineHearings().stream()
                    .findFirst()
                    .map(onlineHearing -> {
                        Name name = firstCase.getData().getAppeal().getAppellant().getName();
                        String nameString = name.getFirstName() + " " + name.getLastName();

                        return new OnlineHearing(
                                onlineHearing.getOnlineHearingId(),
                                nameString,
                                firstCase.getData().getCaseReference(),
                                getDecision(onlineHearing.getOnlineHearingId())
                        );
                    });
        };
    }
}
