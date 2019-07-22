package uk.gov.hmcts.reform.sscscorbackend.service.pdf.util;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.util.I18nBuilder;

public class ActivitiesValidatorTest {
    @Test(expected = IllegalArgumentException.class)
    public void failsIfDailyLivingActivityNotMapped() throws IOException {

        I18nBuilder i18nBuilder = new I18nBuilder();

        OnlineHearing onlineHearing = createOnlineHearing(new Activity("notMappedActivity", "1"), new Activity("movingAround", "0"));
        new ActivitiesValidator(i18nBuilder).validateWeHaveMappingForActivities(onlineHearing);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfMobilityActivityNotMapped() throws IOException {

        I18nBuilder i18nBuilder = new I18nBuilder();

        OnlineHearing onlineHearing = createOnlineHearing(new Activity("makingBudgetingDecisions", "6"), new Activity("notMappedActivity", "0"));
        new ActivitiesValidator(i18nBuilder).validateWeHaveMappingForActivities(onlineHearing);
    }

    @Test
    public void passesIfAllActivitiesAndScoresMapped() throws IOException {
        I18nBuilder i18nBuilder = new I18nBuilder();

        OnlineHearing onlineHearing = createOnlineHearing(new Activity("makingBudgetingDecisions", "6"), new Activity("movingAround", "0"));
        new ActivitiesValidator(i18nBuilder).validateWeHaveMappingForActivities(onlineHearing);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfDailyLivingActivityScoreNotMapped() throws IOException {

        I18nBuilder i18nBuilder = new I18nBuilder();

        OnlineHearing onlineHearing = createOnlineHearing(new Activity("makingBudgetingDecisions", "999"), new Activity("movingAround", "0"));
        new ActivitiesValidator(i18nBuilder).validateWeHaveMappingForActivities(onlineHearing);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfMobilityActivityScoreNotMapped() throws IOException {

        I18nBuilder i18nBuilder = new I18nBuilder();

        OnlineHearing onlineHearing = createOnlineHearing(new Activity("makingBudgetingDecisions", "6"), new Activity("movingAround", "999"));
        new ActivitiesValidator(i18nBuilder).validateWeHaveMappingForActivities(onlineHearing);
    }

    private OnlineHearing createOnlineHearing(Activity dailyLiving, Activity mobility) {
        return new OnlineHearing(
                "hearingId",
                "appellantName",
                "caseRef",
                123456789L,
                new Decision(
                        "sate",
                        "startTime",
                        "appellantReply",
                        "replyDate",
                        "startDate",
                        "endDate",
                        mock(DecisionRates.class),
                        "reason",
                        new Activities(singletonList(dailyLiving), singletonList(mobility))
                ),
                new FinalDecision("reason"),
                true,
                new AppellantDetails(new AddressDetails("line1","line2","town", "county","postcode"), "email", "012", "120")

        );
    }

}