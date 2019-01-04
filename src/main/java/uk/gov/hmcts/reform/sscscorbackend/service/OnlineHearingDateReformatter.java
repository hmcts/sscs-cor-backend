package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.Decision;
import uk.gov.hmcts.reform.sscscorbackend.domain.OnlineHearing;

@Service
public class OnlineHearingDateReformatter {
    public OnlineHearing getReformattedOnlineHearing(OnlineHearing onlineHearing) {
        Decision originalDecision = onlineHearing.getDecision();
        String reformattedStartDate = reformatDate(originalDecision.getStartDate());
        String reformattedEndDate = isNotBlank(originalDecision.getEndDate())
                ? reformatDate(originalDecision.getEndDate()) : originalDecision.getEndDate();

        Decision newDecision = new Decision(
                originalDecision.getDecisionState(),
                originalDecision.getDecisionStateDateTime(),
                originalDecision.getAppellantReply(),
                originalDecision.getAppellantReplyDateTime(),
                reformattedStartDate,
                reformattedEndDate,
                originalDecision.getDecisionRates(),
                originalDecision.getReason(),
                originalDecision.getActivities()
        );
        return new OnlineHearing(
                onlineHearing.getOnlineHearingId(),
                onlineHearing.getAppellantName(),
                onlineHearing.getCaseReference(),
                newDecision
        );
    }

    private String reformatDate(String startDateString) {
        return LocalDate.parse(startDateString).format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }
}
