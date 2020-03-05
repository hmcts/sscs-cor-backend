package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.util.PostcodeUtil;
import uk.gov.hmcts.reform.sscscorbackend.util.UpdateSubscription;

@Slf4j
@Service
public class CitizenLoginService {
    private static final String UPDATED_SSCS = "Updated SSCS";

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
        log.info(format("Find case: Searching for case with tya [%s] for user [%s]", tya, idamTokens.getUserId()));
        List<CaseDetails> caseDetails = corCcdService.searchForCitizen(idamTokens);
        List<SscsCaseDetails> sscsCaseDetails = caseDetails.stream()
                .map(sscsCcdConvertService::getCaseDetails)
                .collect(toList());
        if (!isBlank(tya)) {
            log.info(format("Find case: Filtering for case with tya [%s] for user [%s]", tya, idamTokens.getUserId()));
            List<OnlineHearing> convert = convert(
                    sscsCaseDetails.stream()
                            .filter(casesWithSubscriptionMatchingTya(tya))
                            .collect(toList())
            );
            log.info(format("Find case: Found [%s] cases for tya [%s] for user [%s]", convert.size(), tya, idamTokens.getUserId()));

            return convert;
        }

        log.info(format("Searching for case without for user [%s]", idamTokens.getUserId()));
        List<OnlineHearing> convert = convert(sscsCaseDetails);
        log.info(format("Found [%s] cases without tya for user [%s]", convert.size(), idamTokens.getUserId()));
        return convert;
    }

    private List<OnlineHearing> convert(List<SscsCaseDetails> sscsCaseDetails) {
        return sscsCaseDetails.stream()
                .map(onlineHearingService::loadHearing)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public Optional<OnlineHearing> associateCaseToCitizen(IdamTokens citizenIdamTokens, String tya, String email, String postcode) {
        SscsCaseDetails caseByAppealNumber = corCcdService.findCaseByAppealNumber(tya, idamService.getIdamTokens());

        if (caseByAppealNumber != null) {
            log.info(format("Associate case: Found case to assign id [%s] for tya [%s] email [%s] postcode [%s]", caseByAppealNumber.getId(), tya, email, postcode));
            if (postcodeUtil.hasAppellantPostcode(caseByAppealNumber, postcode)) {
                log.info(format("Associate case: Found case to assign id [%s] for tya [%s] email [%s] postcode [%s] matches postcode", caseByAppealNumber.getId(), tya, email, postcode));
                if (caseHasSubscriptionWithTyaAndEmail(caseByAppealNumber, tya, email)) {
                    log.info(format("Found case to assign id [%s] for tya [%s] email [%s] postcode [%s] has subscription", caseByAppealNumber.getId(), tya, email, postcode));
                    corCcdService.addUserToCase(citizenIdamTokens.getUserId(), caseByAppealNumber.getId());
                    updateCaseWithLastLoggedIntoMya(email, caseByAppealNumber);
                    return onlineHearingService.loadHearing(caseByAppealNumber);
                } else {
                    log.info(format("Associate case: Subscription does not match id [%s] for tya [%s] email [%s] postcode [%s]", caseByAppealNumber.getId(), tya, email, postcode));
                }
            } else {
                log.info(format("Associate case: Postcode does not match id [%s] for tya [%s] email [%s] postcode [%s]", caseByAppealNumber.getId(), tya, email, postcode));
            }
        } else {
            log.info(format("Associate case: No case found for tya [%s] email [%s] postcode [%s]", tya, email, postcode));
        }
        return Optional.empty();
    }

    public void findAndUpdateCaseLastLoggedIntoMya(IdamTokens citizenIdamTokens, String caseId) {
        if (StringUtils.isNotEmpty(caseId)) {
            SscsCaseDetails caseDetails = corCcdService.getByCaseId(Long.valueOf(caseId), idamService.getIdamTokens());
            if (caseDetails != null && caseHasSubscriptionWithMatchingEmail(caseDetails, citizenIdamTokens.getEmail())) {
                updateCaseWithLastLoggedIntoMya(citizenIdamTokens.getEmail(), caseDetails);
            }
        }

    }

    private void updateCaseWithLastLoggedIntoMya(String email, SscsCaseDetails caseByAppealNumber) {
        updateSubscriptionWithLastLoggedIntoMya(caseByAppealNumber, email);
        corCcdService.updateCase(caseByAppealNumber.getData(), caseByAppealNumber.getId(), EventType.UPDATE_CASE_ONLY.getCcdType(), "SSCS - update last logged in MYA", UPDATED_SSCS, idamService.getIdamTokens());
    }

    private Predicate<SscsCaseDetails> casesWithSubscriptionMatchingTya(String tya) {
        return sscsCaseDetails -> {
            Subscriptions subscriptions = sscsCaseDetails.getData().getSubscriptions();

            return of(subscriptions.getAppellantSubscription(), subscriptions.getAppointeeSubscription(), subscriptions.getRepresentativeSubscription())
                    .anyMatch(subscription -> subscription != null && tya.equals(subscription.getTya()));
        };
    }

    private boolean caseHasSubscriptionWithTyaAndEmail(SscsCaseDetails sscsCaseDetails, String tya, String email) {
        Subscriptions subscriptions = sscsCaseDetails.getData().getSubscriptions();

        return of(subscriptions.getAppellantSubscription(), subscriptions.getAppointeeSubscription(), subscriptions.getRepresentativeSubscription())
                .anyMatch(subscription -> subscription != null && tya.equals(subscription.getTya()) && email.equals(subscription.getEmail()));
    }

    private boolean caseHasSubscriptionWithMatchingEmail(SscsCaseDetails sscsCaseDetails, String email) {
        Subscriptions subscriptions = sscsCaseDetails.getData().getSubscriptions();

        return of(subscriptions.getAppellantSubscription(), subscriptions.getAppointeeSubscription(), subscriptions.getRepresentativeSubscription())
                .anyMatch(subscription -> subscription != null && email.equals(subscription.getEmail()));
    }

    private void updateSubscriptionWithLastLoggedIntoMya(SscsCaseDetails sscsCaseDetails, String email) {
        Subscriptions subscriptions = sscsCaseDetails.getData().getSubscriptions();
        String lastLoggedIntoMya = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        if (subscriptions != null && subscriptions.getAppellantSubscription() != null
                && email.equalsIgnoreCase(subscriptions.getAppellantSubscription().getEmail())) {
            final UpdateSubscription.SubscriptionUpdate appellantSubscriptionUpdate =
                new UpdateSubscription.SubscriptionUpdate() {
                    @Override
                    public Subscription getSubscription(Subscriptions subscriptions) {
                        return subscriptions.getAppellantSubscription();
                    }

                    @Override
                    public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                        return sscsCaseDetails.getData().getSubscriptions().toBuilder()
                                .appellantSubscription(subscription).build();
                    }
                };
            UpdateSubscription.updateSubscription(sscsCaseDetails.getData(), appellantSubscriptionUpdate, lastLoggedIntoMya);
        }

        if (subscriptions != null && subscriptions.getAppointeeSubscription() != null
                && email.equalsIgnoreCase(subscriptions.getAppointeeSubscription().getEmail())) {
            final UpdateSubscription.SubscriptionUpdate appointeeSubscriptionUpdate =
                new UpdateSubscription.SubscriptionUpdate() {
                    @Override
                    public Subscription getSubscription(Subscriptions subscriptions) {
                        return subscriptions.getAppointeeSubscription();
                    }

                    @Override
                    public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                        return sscsCaseDetails.getData().getSubscriptions().toBuilder()
                                .appointeeSubscription(subscription).build();
                    }
                };
            UpdateSubscription.updateSubscription(sscsCaseDetails.getData(), appointeeSubscriptionUpdate, lastLoggedIntoMya);
        }

        if (subscriptions != null && subscriptions.getRepresentativeSubscription() != null
                && email.equalsIgnoreCase(subscriptions.getRepresentativeSubscription().getEmail())) {
            final UpdateSubscription.SubscriptionUpdate representativeSubscriptionUpdate =
                new UpdateSubscription.SubscriptionUpdate() {
                    @Override
                    public Subscription getSubscription(Subscriptions subscriptions) {
                        return subscriptions.getRepresentativeSubscription();
                    }

                    @Override
                    public Subscriptions updateExistingSubscriptions(Subscription subscription) {
                        return sscsCaseDetails.getData().getSubscriptions().toBuilder()
                                .representativeSubscription(subscription).build();
                    }
                };
            UpdateSubscription.updateSubscription(sscsCaseDetails.getData(), representativeSubscriptionUpdate, lastLoggedIntoMya);
        }

    }

}
