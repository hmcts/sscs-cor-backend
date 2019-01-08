package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.of;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

@Component
public class DecisionExtractor {

    private static final Map<String, String> activityMappings = new HashMap<>();
    private static final String AWARD = "award";

    static {
        activityMappings.put("preparingFood", "dailyLivingPreparingFood");
        activityMappings.put("takingNutrition", "dailyLivingTakingNutrition");
        activityMappings.put("managingTherapy", "dailyLivingManagingTherapy");
        activityMappings.put("washingBathing", "dailyLivingWashingBathing");
        activityMappings.put("managingToilet", "dailyLivingManagingToilet");
        activityMappings.put("dressingUndressing", "dailyLivingDressingUndressing");
        activityMappings.put("communicatingVerbally", "dailyLivingCommunicatingVerbally");
        activityMappings.put("readingAndUnderstanding", "dailyLivingReadingSigns");
        activityMappings.put("engagingWithOtherPeople", "dailyLivingEngangingPeople");
        activityMappings.put("makingBudgetingDecisions", "dailyLivingMakingBudgetDecisions");
        activityMappings.put("planningFollowingJourneys", "MobilityPlanningJourneys");
        activityMappings.put("movingAround", "MobilityMovingAround");
    }

    private static final List<String> dailyLivingActivities = asList(
            "preparingFood", "takingNutrition", "managingTherapy", "washingBathing", "managingToilet",
            "dressingUndressing", "communicatingVerbally", "readingAndUnderstanding", "engagingWithOtherPeople",
            "makingBudgetingDecisions");
    private static final List<String> mobilityActivities = asList("planningFollowingJourneys", "movingAround");

    public Decision extract(long caseId, CohDecision decision, CohDecisionReply appellantReply) {
        String unescapeJson = StringEscapeUtils.unescapeJava(decision.getDecisionText());

        JSONObject jsonObject = new JSONObject(unescapeJson);
        JSONObject decisionJson = jsonObject.getJSONObject("decisions_SSCS_benefit_" + caseId);

        String startDateString = getDate(decisionJson, "Start");

        String endDateRadio = decisionJson.getString("endDateRadio");
        String endDateString = endDateRadio.equalsIgnoreCase("indefinite") ? null : getDate(decisionJson, "End");
        DecisionRates decisionRates = getDecisionRates(decisionJson);
        String reason = decisionJson.getString("reasonsTribunalView");
        Activities activities = getActivities(decisionJson);

        return new Decision(
                decision.getCurrentDecisionState().getStateName(),
                decision.getCurrentDecisionState().getStateDateTime(),
                appellantReply.getReply().equals("") ? null : appellantReply.getReply(),
                appellantReply.getReplyDateTime().equals("") ? null : appellantReply.getReplyDateTime(),
                startDateString,
                endDateString,
                decisionRates,
                reason,
                activities
        );
    }

    private DecisionRates getDecisionRates(JSONObject decision) {
        Rate forDailyLiving = Rate.valueOf(decision.getString("forDailyLiving"));
        Rate forMobility = Rate.valueOf(decision.getString("forMobility"));
        ComparedRate compareToDwpAward = ComparedRate.valueOf(decision.getString("compareToDWPAward"));
        return new DecisionRates(forDailyLiving, forMobility, compareToDwpAward);
    }

    private Activities getActivities(JSONObject decisionJson) {
        List<Activity> dailyLivingActivitiesList = getActivityList(dailyLivingActivities, decisionJson);
        List<Activity> mobilityActivitiesList = getActivityList(mobilityActivities, decisionJson);

        return new Activities(dailyLivingActivitiesList, mobilityActivitiesList);
    }

    private List<Activity> getActivityList(List<String> activities, JSONObject decisionJson) {
        return activities.stream()
                .filter(decisionJson::getBoolean)
                .map(activityName -> {
                    String selectorName = activityMappings.get(activityName);
                    String selectionKey = decisionJson.getString(selectorName);

                    return new Activity(activityName, selectionKey);
                }).collect(toList());
    }

    private String getDate(JSONObject decision, String dateBoundary) {
        String awardDateDay = decision.getString(AWARD + dateBoundary + "DateDay");
        String awardDateMonth = decision.getString(AWARD + dateBoundary + "DateMonth");
        String awardDateYear = decision.getString(AWARD + dateBoundary + "DateYear");
        LocalDate startDate = of(parseInt(awardDateYear), parseInt(awardDateMonth), parseInt(awardDateDay));

        return startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
