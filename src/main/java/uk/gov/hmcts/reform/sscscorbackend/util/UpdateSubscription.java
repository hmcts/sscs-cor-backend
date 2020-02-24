package uk.gov.hmcts.reform.sscscorbackend.util;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;

@Slf4j
public class UpdateSubscription {

    private static final String YES = "Yes";
    private static final String NO = "No";

    private UpdateSubscription() {
        // Empty
    }

    public static void updateSubscription(SscsCaseData existingCcdCaseData,
                                   SubscriptionUpdate subscriptionUpdate,
                                   String lastLoggedIntoMya) {

        Subscriptions existingSubscriptions = existingCcdCaseData.getSubscriptions();

        Subscription existingSubscription = subscriptionUpdate.getSubscription(existingSubscriptions);

        Subscription updatedSubscription =
                keepExistingSubscribedSubscriptions(existingSubscription, lastLoggedIntoMya);

        existingSubscriptions = subscriptionUpdate.updateExistingSubscriptions(updatedSubscription);

        existingCcdCaseData.setSubscriptions(existingSubscriptions);
    }

    private static Subscription keepExistingSubscribedSubscriptions(Subscription exisitingSubscription,
                                                                    String lastLoggedIntoMya) {
        return Subscription.builder()
            .wantSmsNotifications(exisitingSubscription.getWantSmsNotifications())
            .email(exisitingSubscription.getEmail())
            .subscribeSms(exisitingSubscription.isSmsSubscribed() ? YES : NO)
            .subscribeEmail(exisitingSubscription.isEmailSubscribed() ? YES : NO)
            .mobile(exisitingSubscription.getMobile())
            .tya(exisitingSubscription.getTya())
            .lastLoggedIntoMya(lastLoggedIntoMya)
            .build();
    }

    public interface SubscriptionUpdate {

        Subscription getSubscription(Subscriptions subscriptions);

        Subscriptions updateExistingSubscriptions(Subscription subscription);
    }
}
