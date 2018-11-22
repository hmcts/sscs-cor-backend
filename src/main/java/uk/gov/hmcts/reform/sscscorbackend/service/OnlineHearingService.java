package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

        if (cases == null || cases.isEmpty()) {
            return Optional.empty();
        }
        List<SscsCaseDetails> corCases = filterCorCases(cases);

        if (corCases.size() == 0) {
            throw new CaseNotCorException();
        } else if (corCases.size() > 1) {
            throw new IllegalStateException("Multiple appeals with online hearings found.");
        }

        return loadOnlineHearingFromCoh(corCases.get(0));
    }

    private List<SscsCaseDetails> filterCorCases(List<SscsCaseDetails> cases) {
        final AtomicInteger counter = new AtomicInteger(1);
        return cases.stream().filter(caseDetails -> caseDetails.getData() != null &&
                caseDetails.getData().getAppeal() != null &&
                HEARING_TYPE_ONLINE_RESOLUTION.equalsIgnoreCase(caseDetails.getData().getAppeal().getHearingType())
        )
                .peek(caseDetails -> LOG.info(counter.getAndIncrement() + ") case id: " + caseDetails.getId()))
                .collect(toList());
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
                    .collect(toList());

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

    private Optional<OnlineHearing> loadOnlineHearingFromCoh(SscsCaseDetails sscsCaseDeails) {
        CohOnlineHearings cohOnlineHearings = cohClient.getOnlineHearing(sscsCaseDeails.getId());

        return cohOnlineHearings.getOnlineHearings().stream()
                .findFirst()
                .map(onlineHearing -> {
                    Name name = sscsCaseDeails.getData().getAppeal().getAppellant().getName();
                    String nameString = name.getFirstName() + " " + name.getLastName();

                    return new OnlineHearing(
                            onlineHearing.getOnlineHearingId(),
                            nameString,
                            sscsCaseDeails.getData().getCaseReference(),
                            getDecision(onlineHearing.getOnlineHearingId())
                    );
                });
    }
}
