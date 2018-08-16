package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answer {
    @ApiModelProperty(example = "this is an answer to a question", required = true)
    @JsonProperty(value = "answer")
    private String answer;

    // needed for Jackson
    private Answer() {
    }

    public Answer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}
