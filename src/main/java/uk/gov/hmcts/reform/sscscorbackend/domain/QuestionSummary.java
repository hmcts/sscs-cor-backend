package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionSummary {
    private final String id;
    private final String questionHeaderText;

    public QuestionSummary(String id, String questionHeaderText) {
        this.id = id;
        this.questionHeaderText = questionHeaderText;
    }

    @ApiModelProperty(example = "question-Id", required = true)
    @JsonProperty(value = "question_id")
    public String getId() {
        return id;
    }

    @ApiModelProperty(example = "A question header", required = true)
    @JsonProperty(value = "question_header_text")
    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuestionSummary that = (QuestionSummary) o;
        return Objects.equals(questionHeaderText, that.questionHeaderText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionHeaderText);
    }

    @Override
    public String toString() {
        return "QuestionSummary{" +
                "questionHeaderText='" + questionHeaderText + '\'' +
                '}';
    }
}
