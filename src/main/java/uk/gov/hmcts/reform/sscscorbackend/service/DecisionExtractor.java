package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.of;
import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

@Component
public class DecisionExtractor {
    public Decision extract(long caseId, CohDecision decision, CohDecisionReply appellantReply) {
        String unescapeJson = StringEscapeUtils.unescapeJava(decision.getDecisionText());

        System.out.println(unescapeJson);

        JSONObject jsonObject = new JSONObject(unescapeJson);
        JSONObject decisionJson = jsonObject.getJSONObject("decisions_SSCS_benefit_" + caseId);

        String startDateString = getDate(decisionJson, "Start");
        String endDateString = getDate(decisionJson, "End");
        DecisionRates decisionRates = getDecisionRates(decisionJson);
        String reason = decisionJson.getString("reasonsTribunalView");
        Activities activities = getActivities();

        return new Decision(
                decision.getCurrentDecisionState().getStateName(),
                decision.getCurrentDecisionState().getStateDateTime(),
                appellantReply.getReply(),
                appellantReply.getReplyDateTime(),
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

    private Activities getActivities() {
        return new Activities(
                    asList(new Activity("dailyActivity1", 10), new Activity("dailyActivity2", 5)),
                    asList(new Activity("mobilityActivity1", 10), new Activity("mobilityActivity2", 7)));
    }

    private String getDate(JSONObject decision, String dateBoundary) {
        String awardDateDay = decision.getString("award" + dateBoundary + "DateDay");
        String awardDateMonth = decision.getString("award" + dateBoundary + "DateMonth");
        String awardDateYear = decision.getString("award" + dateBoundary + "DateYear");
        LocalDate startDate = of(parseInt(awardDateYear), parseInt(awardDateMonth), parseInt(awardDateDay));

        return startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
