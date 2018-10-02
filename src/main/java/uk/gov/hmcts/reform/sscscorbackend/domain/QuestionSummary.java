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
    private final int questionOrdinal;
    private final String questionHeaderText;
    private final AnswerState answerState;

    public QuestionSummary(String id, int questionOrdinal, String questionHeaderText, AnswerState answerState) {
        this.id = id;
        this.questionHeaderText = questionHeaderText;
        this.answerState = answerState;
        this.questionOrdinal = questionOrdinal;
    }

    @ApiModelProperty(example = "question-Id", required = true)
    @JsonProperty(value = "question_id")
    public String getId() {
        return id;
    }

    @ApiModelProperty(example = "1", required = true)
    @JsonProperty(value = "question_ordinal")
    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    @ApiModelProperty(example = "A question header", required = true)
    @JsonProperty(value = "question_header_text")
    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    @ApiModelProperty(required = true)
    @JsonProperty(value = "answer_state")
    public AnswerState getAnswerState() {
        return answerState;
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
                "id='" + id + '\'' +
                ", questionOrdinal='" + questionOrdinal + '\'' +
                ", questionHeaderText='" + questionHeaderText + '\'' +
                ", answerState=" + answerState +
                '}';
    }
}
