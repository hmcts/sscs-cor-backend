package uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CohConversation {
    private List<CohQuestion> questions;
    private CohRelisting relisting;


    public CohConversation(
            @JsonProperty(value = "questions")List<CohQuestion> cohQuestions,
            @JsonProperty(value = "relisting")CohRelisting cohRelisting
    ) {
        this.questions = cohQuestions;
        this.relisting = cohRelisting;
    }

    public List<CohQuestion> getQuestions() {
        return questions;
    }

    public CohRelisting getRelisting() {
        return relisting;
    }
}
