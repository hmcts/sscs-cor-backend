package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.email.DecisionEmailService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CaseDetails;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.apinotifications.CcdEvent;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.DecisionExtractor;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.*;

@Slf4j
@Service
public class OnlineHearingService {
    private static final String HEARING_TYPE_ONLINE_RESOLUTION = "cor";

    private final CohService cohClient;
    private final CorCcdService ccdService;
    private final IdamService idamService;
    private final DecisionExtractor decisionExtractor;
    private final AmendPanelMembersService amendPanelMembersService;
    private final boolean enableSelectByCaseId;
    private final DecisionEmailService decisionEmailService;

    public OnlineHearingService(
            @Autowired CohService cohService,
            @Autowired CorCcdService ccdService,
            @Autowired IdamService idamService,
            @Autowired DecisionExtractor decisionExtractor,
            @Autowired AmendPanelMembersService amendPanelMembersService,
            @Value("${enable_select_by_case_id}") boolean enableSelectByCaseId,
            @Autowired DecisionEmailService decisionEmailService) {
        this.cohClient = cohService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.decisionExtractor = decisionExtractor;
        this.amendPanelMembersService = amendPanelMembersService;
        this.enableSelectByCaseId = enableSelectByCaseId;
        this.decisionEmailService = decisionEmailService;
    }

    public boolean createOnlineHearing(CcdEvent ccdEvent) {
        CaseDetails newCaseDetails = ccdEvent.getCaseDetails();
        String caseId = newCaseDetails.getCaseId();
        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId);

        boolean createdOnlineHearing = cohClient.createOnlineHearing(createOnlineHearingRequest);

        amendPanelMembersService.amendPanelMembersPermissions(ccdEvent);

        return createdOnlineHearing;
    }

    public Optional<OnlineHearing> getOnlineHearing(String emailAddress) {
        String[] splitEmailAddress = emailAddress.split("\\+");
        String actualEmailAddress = enableSelectByCaseId ? splitEmailAddress[0] : emailAddress;
        IdamTokens idamTokens = idamService.getIdamTokens();
        log.info("Got idam tokens");

        List<SscsCaseDetails> cases = ccdService.findCaseBy(
                ImmutableMap.of("case.subscriptions.appellantSubscription.email", actualEmailAddress),
                idamTokens
        );
        log.info("Found {} cases", cases.size());

        if (cases == null || cases.isEmpty()) {
            return Optional.empty();
        }
        List<SscsCaseDetails> corCases = filterCorCases(cases);

        if (corCases.isEmpty()) {
            throw new CaseNotCorException();
        } else if (corCases.size() > 1) {
            if (enableSelectByCaseId && splitEmailAddress.length > 1) {
                Optional<SscsCaseDetails> caseForId = corCases.stream()
                        .filter(corCase -> corCase.getId().equals(Long.valueOf(splitEmailAddress[1]))).findFirst();

                return loadOnlineHearingFromCoh(caseForId.orElseThrow(() -> new IllegalStateException("Multiple appeals with online hearings found.")));
            } else {
                throw new IllegalStateException("Multiple appeals with online hearings found.");
            }
        }

        return loadOnlineHearingFromCoh(corCases.get(0));
    }

    private List<SscsCaseDetails> filterCorCases(List<SscsCaseDetails> cases) {
        return cases.stream().filter(caseDetails -> caseDetails.getData() != null &&
                caseDetails.getData().getAppeal() != null &&
                HEARING_TYPE_ONLINE_RESOLUTION.equalsIgnoreCase(caseDetails.getData().getAppeal().getHearingType())
        )
                .collect(toList());
    }

    public Optional<Long> getCcdCaseId(String onlineHearingId) {
        CohOnlineHearing onlineHearing = cohClient.getOnlineHearing(onlineHearingId);
        return (onlineHearing != null) ? Optional.of(onlineHearing.getCcdCaseId()) : Optional.empty();
    }

    public Optional<SscsCaseDetails> getCcdCase(String onlineHearingId) {
        return getCcdCaseId(onlineHearingId).map(caseId -> {
            IdamTokens idamTokens = idamService.getIdamTokens();
            SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);

            if (caseDetails == null) {
                throw new IllegalStateException("Online hearing for ccdCaseId [" + caseId + "] found but cannot find the case in CCD");
            }

            return caseDetails;
        });
    }

    public void addDecisionReply(String onlineHearingId, TribunalViewResponse tribunalViewResponse) {
        CohDecisionReply cohDecisionReply = new CohDecisionReply(tribunalViewResponse.getReply(), tribunalViewResponse.getReason());
        cohClient.addDecisionReply(onlineHearingId, cohDecisionReply);

        Optional<Long> ccdCaseIdOptional = getCcdCaseId(onlineHearingId);
        Long caseId = ccdCaseIdOptional.orElseThrow(() -> new IllegalArgumentException("Cannot find online hearing id to add decision [" + onlineHearingId + "]"));
        SscsCaseDetails sscsCaseDetails = ccdService.getByCaseId(caseId, idamService.getIdamTokens());
        decisionEmailService.sendEmail(sscsCaseDetails, tribunalViewResponse);
    }

    private CohDecisionReply getAppellantDecisionReply(String onlineHearingId) {
        Optional<CohDecisionReplies> decisionRepliesWrapper = cohClient.getDecisionReplies(onlineHearingId);
        if (decisionRepliesWrapper.isPresent()) {
            List<CohDecisionReply> decisionReplies = decisionRepliesWrapper.get().getDecisionReplies()
                    .stream()
                    //.filter(d -> d.getAuthorReference().equals("oauth2Token"))
                    .collect(toList());

            if (decisionReplies.size() > 0) {
                return decisionReplies.get(0);
            }
        }
        return new CohDecisionReply("", "", "", "");
    }

    private Decision getDecision(String onlineHearingId, long caseId) {
        Optional<CohDecision> decision = cohClient.getDecision(onlineHearingId);
        CohDecisionReply appellantReply = getAppellantDecisionReply(onlineHearingId);

        return decision.map(d -> decisionExtractor.extract(caseId, d, appellantReply))
                .orElse(null);
    }

    public Optional<OnlineHearing> loadOnlineHearingFromCoh(SscsCaseDetails sscsCaseDeails) {
        CohOnlineHearings cohOnlineHearings = cohClient.getOnlineHearing(sscsCaseDeails.getId());

        return cohOnlineHearings.getOnlineHearings().stream()
                .findFirst()
                .map(onlineHearing -> {
                    Name name = sscsCaseDeails.getData().getAppeal().getAppellant().getName();
                    String nameString = name.getFirstName() + " " + name.getLastName();

                    boolean hasFinalDecision = sscsCaseDeails.getData().isCorDecision();

                    return new OnlineHearing(
                            onlineHearing.getOnlineHearingId(),
                            nameString,
                            sscsCaseDeails.getData().getCaseReference(),
                            sscsCaseDeails.getId(),
                            getDecision(onlineHearing.getOnlineHearingId(), sscsCaseDeails.getId()),
                            new FinalDecision(sscsCaseDeails.getData().getDecisionNotes()), hasFinalDecision);
                });
    }

    public Optional<OnlineHearing> loadHearing(SscsCaseDetails sscsCaseDeails) {
        SscsCaseData data = sscsCaseDeails.getData();
        HearingOptions hearingOptions = data.getAppeal().getHearingOptions();
        Name name = data.getAppeal().getAppellant().getName();
        String nameString = name.getFirstName() + " " + name.getLastName();

        return Optional.of(loadOnlineHearingFromCoh(sscsCaseDeails)
                .orElseGet(() -> {
                    List<String> arrangements = (hearingOptions.getArrangements() != null)
                            ? hearingOptions.getArrangements() : emptyList();
                    return new OnlineHearing(
                            nameString,
                            sscsCaseDeails.getData().getCaseReference(),
                            sscsCaseDeails.getId(),
                            new HearingArrangements(
                                    "yes".equalsIgnoreCase(hearingOptions.getLanguageInterpreter()),
                                    hearingOptions.getLanguages(),
                                    arrangements.contains("signLanguageInterpreter"),
                                    hearingOptions.getSignLanguageType(),
                                    arrangements.contains("hearingLoop"),
                                    arrangements.contains("disabledAccess"),
                                    hearingOptions.getOther()
                            ));
                }));
    }
}
