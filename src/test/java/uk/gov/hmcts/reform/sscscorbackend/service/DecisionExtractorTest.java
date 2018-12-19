package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

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
            "      \\\"approveDraftConsent\\\":\\\"indefinite\\\",\n" +
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

    //todo handle line breaks in reasons
    @Test
    public void extractDecision() {
        String decisionsState = "decisionsState";
        String decisionsStartDateTime = "decisionsStartDateTime";
        String appellantReply = "appellantReply";
        String appellantReplyDateTime = "appellantReplyDateTime";

        CohDecision cohDecision = new CohDecision(
                "onlineHearingId", "", "", "", decisionString, new CohState(decisionsState, decisionsStartDateTime)
        );
        CohDecisionReply cohDecisionReply = new CohDecisionReply(
                appellantReply, "reason", appellantReplyDateTime, "authorRef"
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
}