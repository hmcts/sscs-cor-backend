package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answer {

    @ApiModelProperty(notes = "Answer state can be either 'draft' or 'submitted' defaults to 'draft' if not set")
    @JsonProperty(value = "answer_state")
    private AnswerState answerState = AnswerState.draft;

    @ApiModelProperty(example = "this is an answer to a question", required = true)
    @JsonProperty(value = "answer")
    private String answer;

    // needed for Jackson
    private Answer() {
    }

    public Answer(AnswerState answerState, String answer) {
        this.answerState = answerState;
        this.answer = answer;
    }

    public AnswerState getAnswerState() {
        return answerState;
    }

    public String getAnswer() {
        return answer;
    }
}
