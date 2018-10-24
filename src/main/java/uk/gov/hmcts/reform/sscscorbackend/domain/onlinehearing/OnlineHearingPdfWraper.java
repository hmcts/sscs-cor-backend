package uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestionRounds;

@Value
@Builder
public class OnlineHearingPdfWraper {
    private String appellantName;
    private String caseReference;
    private String nino;
    private CohQuestionRounds cohQuestionRounds;
}
