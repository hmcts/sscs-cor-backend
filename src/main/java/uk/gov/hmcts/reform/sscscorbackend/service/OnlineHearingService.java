package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;

@Service
public class OnlineHearingService {

    private static final Logger LOG = getLogger(RestResponseEntityExceptionHandler.class);
    private static final String HEARING_TYPE_ONLINE_RESOLUTION = "cor";

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

        final AtomicInteger counter = new AtomicInteger(1);
        return cases.stream()
                .filter(caseDetails -> caseDetails.getData() != null &&
                        caseDetails.getData().getAppeal() != null &&
                        HEARING_TYPE_ONLINE_RESOLUTION.equalsIgnoreCase(caseDetails.getData().getAppeal().getHearingType())
                )
                .peek(caseDetails -> {
                    LOG.info(counter.getAndIncrement() + ") case id: " + caseDetails.getId());
                })
                .reduce(checkThereIsOnlyOneCase())
                .flatMap(getHearingFromCoh());
    }

    public Optional<Long> getCcdCaseId(String onlineHearingId) {
        CohOnlineHearing onlineHearing = cohClient.getOnlineHearing(onlineHearingId);
        return (onlineHearing != null) ? Optional.of(onlineHearing.getCcdCaseId()) : Optional.empty();
    }

    public void addDecisionReply(String onlineHearingId, TribunalViewResponse tribunalViewResponse) {
        CohDecisionReply cohDecisionReply = new CohDecisionReply(tribunalViewResponse.getReply(), tribunalViewResponse.getReason());
        cohClient.addDecisionReply(onlineHearingId, cohDecisionReply);
    }

    private CohDecisionReply getAppellantDecisionReply(String onlineHearingId) {
        Optional<CohDecisionReplies> decisionRepliesWrapper = cohClient.getDecisionReplies(onlineHearingId);
        if (decisionRepliesWrapper.isPresent()) {
            List<CohDecisionReply> decisionReplies = decisionRepliesWrapper.get().getDecisionReplies()
                    .stream()
                    .filter(d -> d.getAuthorReference().equals("oauth2Token"))
                    .collect(Collectors.toList());

            if (decisionReplies.size() > 0) {
                return decisionReplies.get(0);
            }
        }
        return new CohDecisionReply("", "", "", "");
    }

    private Decision getDecision(String onlineHearingId) {
        Optional<CohDecision> decision = cohClient.getDecision(onlineHearingId);
        CohDecisionReply appellantReply = getAppellantDecisionReply(onlineHearingId);
        return decision.map(d -> new Decision(onlineHearingId, d.getDecisionAward(),
                    d.getDecisionHeader(), d.getDecisionReason(),
                    d.getDecisionText(), d.getCurrentDecisionState().getStateName(),
                    d.getCurrentDecisionState().getStateDateTime(),
                    appellantReply.getReply(), appellantReply.getReplyDateTime()))
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
