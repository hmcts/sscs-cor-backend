package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.DecisionExtractor;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohDecision;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohDecisionReply;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.CohState;

public class DecisionExtractorTest {
    private static final long caseId = 1234321L;
    private static final String decisionString = "{  \n" +
            "   \\\"decisions_SSCS_benefit_1234321\\\":{  \n" +
            "      \\\"preliminaryView\\\":\\\"yes\\\",\n" +
            "      \\\"visitedPages\\\":{  \n" +
            "         \\\"create\\\":true,\n" +
            "         \\\"preliminary-advanced\\\":true,\n" +
            "         \\\"set-award-dates\\\":true,\n" +
            "         \\\"scores\\\":true,\n" +
            "         \\\"budgeting-decisions\\\":true,\n" +
            "         \\\"planning-journeys\\\":true\n" +
            "      },\n" +
            "      \\\"forDailyLiving\\\":\\\"noAward\\\",\n" +
            "      \\\"forMobility\\\":\\\"enhancedRate\\\",\n" +
            "      \\\"compareToDWPAward\\\":\\\"Higher\\\",\n" +
            "      \\\"awardEndDateDay\\\":\\\"11\\\",\n" +
            "      \\\"awardEndDateMonth\\\":\\\"12\\\",\n" +
            "      \\\"awardEndDateYear\\\":\\\"2018\\\",\n" +
            "      \\\"endDateRadio\\\":\\\"endDate\\\",\n" +
            "      \\\"preparingFood\\\":false,\n" +
            "      \\\"takingNutrition\\\":false,\n" +
            "      \\\"managingTherapy\\\":false,\n" +
            "      \\\"washingBathing\\\":false,\n" +
            "      \\\"managingToilet\\\":false,\n" +
            "      \\\"dressingUndressing\\\":false,\n" +
            "      \\\"communicatingVerbally\\\":false,\n" +
            "      \\\"readingAndUnderstanding\\\":false,\n" +
            "      \\\"engagingWithOtherPeople\\\":false,\n" +
            "      \\\"makingBudgetingDecisions\\\":true,\n" +
            "      \\\"planningFollowingJourneys\\\":true,\n" +
            "      \\\"movingAround\\\":false,\n" +
            "      \\\"dailyLivingMakingBudgetDecisions\\\":\\\"6\\\",\n" +
            "      \\\"MobilityPlanningJourneys\\\":\\\"12\\\",\n" +
            "      \\\"reasonsTribunalView\\\":\\\"There was a reason!\\\",\n" +
            "      \\\"awardStartDateDay\\\":\\\"1\\\",\n" +
            "      \\\"awardStartDateMonth\\\":\\\"4\\\",\n" +
            "      \\\"awardStartDateYear\\\":\\\"2017\\\"\n" +
            "   }\n" +
            "}";

    private static final String decisionWithNoEndDateString = "{  \n" +
            "   \\\"decisions_SSCS_benefit_1234321\\\":{  \n" +
            "      \\\"preliminaryView\\\":\\\"yes\\\",\n" +
            "      \\\"visitedPages\\\":{  \n" +
            "         \\\"create\\\":true,\n" +
            "         \\\"preliminary-advanced\\\":true,\n" +
            "         \\\"set-award-dates\\\":true,\n" +
            "         \\\"scores\\\":true,\n" +
            "         \\\"budgeting-decisions\\\":true,\n" +
            "         \\\"planning-journeys\\\":true\n" +
            "      },\n" +
            "      \\\"forDailyLiving\\\":\\\"noAward\\\",\n" +
            "      \\\"forMobility\\\":\\\"enhancedRate\\\",\n" +
            "      \\\"compareToDWPAward\\\":\\\"Higher\\\",\n" +
            "      \\\"awardEndDateDay\\\":\\\"null\\\",\n" +
            "      \\\"awardEndDateMonth\\\":\\\"null\\\",\n" +
            "      \\\"awardEndDateYear\\\":\\\"null\\\",\n" +
            "      \\\"endDateRadio\\\":\\\"indefinite\\\",\n" +
            "      \\\"preparingFood\\\":false,\n" +
            "      \\\"takingNutrition\\\":false,\n" +
            "      \\\"managingTherapy\\\":false,\n" +
            "      \\\"washingBathing\\\":false,\n" +
            "      \\\"managingToilet\\\":false,\n" +
            "      \\\"dressingUndressing\\\":false,\n" +
            "      \\\"communicatingVerbally\\\":false,\n" +
            "      \\\"readingAndUnderstanding\\\":false,\n" +
            "      \\\"engagingWithOtherPeople\\\":false,\n" +
            "      \\\"makingBudgetingDecisions\\\":true,\n" +
            "      \\\"planningFollowingJourneys\\\":true,\n" +
            "      \\\"movingAround\\\":false,\n" +
            "      \\\"dailyLivingMakingBudgetDecisions\\\":\\\"6\\\",\n" +
            "      \\\"MobilityPlanningJourneys\\\":\\\"12\\\",\n" +
            "      \\\"reasonsTribunalView\\\":\\\"There was a reason!\\\",\n" +
            "      \\\"awardStartDateDay\\\":\\\"1\\\",\n" +
            "      \\\"awardStartDateMonth\\\":\\\"4\\\",\n" +
            "      \\\"awardStartDateYear\\\":\\\"2017\\\"\n" +
            "   }\n" +
            "}";
    private final String decisionsState = "decisionsState";
    private final String decisionsStartDateTime = "decisionsStartDateTime";
    private final String appellantReply = "appellantReply";
    private final String appellantReplyDateTime = "appellantReplyDateTime";
    private final CohDecisionReply cohDecisionReply = new CohDecisionReply(
            appellantReply, "reason", appellantReplyDateTime, "authorRef"
    );


    //todo handle line breaks in reasons
    @Test
    public void extractDecision() {
        CohDecision cohDecision = new CohDecision(
                "onlineHearingId", "", "", "", decisionString, new CohState(decisionsState, decisionsStartDateTime)
        );
        Decision decision = new DecisionExtractor().extract(caseId, cohDecision, cohDecisionReply);

        assertThat(decision, is(new Decision(
                decisionsState,
                decisionsStartDateTime,
                appellantReply,
                appellantReplyDateTime,
                "2017-04-01",
                "2018-12-11",
                new DecisionRates(Rate.noAward, Rate.enhancedRate, ComparedRate.Higher),
                "There was a reason!",
                new Activities(
                        asList(new Activity("makingBudgetingDecisions", "6")),
                        asList(new Activity("planningFollowingJourneys", "12"))
                ))
        ));
    }

    @Test
    public void extractDecisionWithNoEndDate() {
        CohDecision cohDecision = new CohDecision(
                "onlineHearingId", "", "", "", decisionWithNoEndDateString, new CohState(decisionsState, decisionsStartDateTime)
        );
        Decision decision = new DecisionExtractor().extract(caseId, cohDecision, cohDecisionReply);

        assertThat(decision, is(new Decision(
                decisionsState,
                decisionsStartDateTime,
                appellantReply,
                appellantReplyDateTime,
                "2017-04-01",
                null,
                new DecisionRates(Rate.noAward, Rate.enhancedRate, ComparedRate.Higher),
                "There was a reason!",
                new Activities(
                        asList(new Activity("makingBudgetingDecisions", "6")),
                        asList(new Activity("planningFollowingJourneys", "12"))
                ))
        ));
    }
}