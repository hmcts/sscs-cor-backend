package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.util.PostcodeUtil;

@Service
public class CitizenLoginService {
    private final CorCcdService corCcdService;
    private final SscsCcdConvertService sscsCcdConvertService;
    private final IdamService idamService;
    private final PostcodeUtil postcodeUtil;
    private final OnlineHearingService onlineHearingService;

    public CitizenLoginService(CorCcdService corCcdService, SscsCcdConvertService sscsCcdConvertService, IdamService idamService, PostcodeUtil postcodeUtil, OnlineHearingService onlineHearingService) {
        this.corCcdService = corCcdService;
        this.sscsCcdConvertService = sscsCcdConvertService;
        this.idamService = idamService;
        this.postcodeUtil = postcodeUtil;
        this.onlineHearingService = onlineHearingService;
    }

    public List<OnlineHearing> findCasesForCitizen(IdamTokens idamTokens, String tya) {
        List<CaseDetails> caseDetails = corCcdService.searchForCitizen(idamTokens);
        List<SscsCaseDetails> sscsCaseDetails = caseDetails.stream()
                .map(sscsCcdConvertService::getCaseDetails)
                .collect(toList());
        if (!isBlank(tya)) {
            return convert(
                    sscsCaseDetails.stream()
                            .filter(casesWithSubscriptionMatchingTya(tya))
                            .collect(toList())
            );
        }

        return convert(sscsCaseDetails);
    }

    private List<OnlineHearing> convert(List<SscsCaseDetails> sscsCaseDetails) {
        return sscsCaseDetails.stream()
                .map(onlineHearingService::loadOnlineHearingFromCoh)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public Optional<OnlineHearing> associateCaseToCitizen(IdamTokens citizenIdamTokens, String tya, String email, String postcode) {
        SscsCaseDetails caseByAppealNumber = corCcdService.findCaseByAppealNumber(tya, idamService.getIdamTokens());

        if (caseByAppealNumber != null && postcodeUtil.hasAppellantPostcode(caseByAppealNumber, postcode) && caseHasSubscriptionWithTyaAndEmail(caseByAppealNumber, tya, email)) {
            corCcdService.addUserToCase(citizenIdamTokens.getUserId(), caseByAppealNumber.getId());

            return onlineHearingService.loadOnlineHearingFromCoh(caseByAppealNumber);
        } else {
            return Optional.empty();
        }
    }

    private Predicate<SscsCaseDetails> casesWithSubscriptionMatchingTya(String tya) {
        return sscsCaseDetails -> {
            Subscriptions subscriptions = sscsCaseDetails.getData().getSubscriptions();

            return of(subscriptions.getAppellantSubscription(), subscriptions.getAppointeeSubscription(), subscriptions.getRepresentativeSubscription())
                    .anyMatch(subscription -> subscription != null && subscription.getTya().equals(tya));
        };
    }

    private boolean caseHasSubscriptionWithTyaAndEmail(SscsCaseDetails sscsCaseDetails, String tya, String email) {
        Subscriptions subscriptions = sscsCaseDetails.getData().getSubscriptions();

        return of(subscriptions.getAppellantSubscription(), subscriptions.getAppointeeSubscription(), subscriptions.getRepresentativeSubscription())
                .anyMatch(subscription -> subscription != null && tya.equals(subscription.getTya()) && email.equals(subscription.getEmail()));
    }

}
