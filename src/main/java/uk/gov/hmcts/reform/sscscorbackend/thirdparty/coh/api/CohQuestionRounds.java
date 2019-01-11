package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohQuestionRounds {
    private int currentQuestionRound;
    private List<CohQuestionRound> cohQuestionRound;


    public CohQuestionRounds(@JsonProperty(value = "current_question_round") int currentQuestionRound,
                             @JsonProperty(value = "question_rounds") List<CohQuestionRound> cohQuestionRound) {
        this.currentQuestionRound = currentQuestionRound;
        this.cohQuestionRound = cohQuestionRound;
    }

    public int getCurrentQuestionRound() {
        return currentQuestionRound;
    }

    public List<CohQuestionRound> getCohQuestionRound() {
        return cohQuestionRound;
    }
}
