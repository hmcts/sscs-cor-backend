package uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestionRounds;


public class OnlineHearingPdfWraper {
    private String appellantTitle;
    private String appellantFirstName;
    private String appellantLastName;
    private String caseReference;
    private String nino;
    private CohQuestionRounds cohQuestionRounds;

    public OnlineHearingPdfWraper(@JsonProperty(value = "appellant_title") String appellantTitle,
                                  @JsonProperty(value = "appellant_first_name") String appellantFirstName,
                                  @JsonProperty(value = "appellant_last_name") String appellantLastName,
                                  @JsonProperty(value = "case_reference") String caseReference,
                                  @JsonProperty(value = "nino") String nino,
                                  @JsonProperty(value = "coh_question_rounds") CohQuestionRounds cohQuestionRounds) {
        this.appellantTitle = appellantTitle;
        this.appellantTitle = appellantFirstName;
        this.appellantLastName = appellantLastName;
        this.caseReference = caseReference;
        this.nino = nino;
        this.cohQuestionRounds = cohQuestionRounds;


    }
}
