package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {

    private final String onlineHearingId;
    private final String questionId;
    private final String questionHeaderText;
    private final String questionBodyText;
    private final String answer;
    private final AnswerState answerState;
    private final String answerDate;

    public Question(String onlineHearingId,
                    String questionId,
                    String questionHeaderText,
                    String questionBodyText) {
        this.onlineHearingId = onlineHearingId;
        this.questionId = questionId;
        this.questionHeaderText = questionHeaderText;
        this.questionBodyText = questionBodyText;
        this.answer = null;
        this.answerState = AnswerState.unanswered;
        this.answerDate = null;
    }

    public Question(String onlineHearingId,
                    String questionId,
                    String questionHeaderText,
                    String questionBodyText,
                    String answer,
                    AnswerState answerState,
                    String answerDate) {
        this.onlineHearingId = onlineHearingId;
        this.questionId = questionId;
        this.questionHeaderText = questionHeaderText;
        this.questionBodyText = questionBodyText;
        this.answer = answer;
        this.answerState = answerState;
        this.answerDate = answerDate;
    }

    @ApiModelProperty(example = "ID_1", required = true)
    @JsonProperty(value = "online_hearing_id")
    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    @ApiModelProperty(example = "ID_1", required = true)
    @JsonProperty(value = "question_id")
    public String getQuestionId() {
        return questionId;
    }

    @ApiModelProperty(example = "A question header", required = true)
    @JsonProperty(value = "question_header_text")
    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    @ApiModelProperty(example = "A question body", required = true)
    @JsonProperty(value = "question_body_text")
    public String getQuestionBodyText() {
        return questionBodyText;
    }

    @ApiModelProperty(example = "An answer to a question")
    @JsonProperty(value = "answer")
    public String getAnswer() {
        return answer;
    }

    @ApiModelProperty(required = true)
    @JsonProperty(value = "answer_state")
    public AnswerState getAnswerState() {
        return answerState;
    }

    @ApiModelProperty(required = true)
    @JsonProperty(value = "answer_date")
    public String getAnswerDate() {
        return answerDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Question question = (Question) o;
        return Objects.equals(onlineHearingId, question.onlineHearingId) &&
                Objects.equals(questionId, question.questionId) &&
                Objects.equals(questionHeaderText, question.questionHeaderText) &&
                Objects.equals(questionBodyText, question.questionBodyText) &&
                Objects.equals(answer, question.answer) &&
                answerState == question.answerState &&
                Objects.equals(answerDate, question.answerDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onlineHearingId, questionId, questionHeaderText, questionBodyText, answer, answerState, answerDate);
    }

    @Override
    public String toString() {
        return "Question{" +
                "onlineHearingId='" + onlineHearingId + '\'' +
                ", questionId='" + questionId + '\'' +
                ", questionHeaderText='" + questionHeaderText + '\'' +
                ", questionBodyText='" + questionBodyText + '\'' +
                ", answer='" + answer + '\'' +
                ", answerState=" + answerState +
                ", answerDate='" + answerDate + '\'' +
                '}';
    }
}
