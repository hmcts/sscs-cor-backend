package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

public class Question {

    private final String onlineHearingId;
    private final String questionId;
    private final String questionHeaderText;
    private final String questionBodyText;

    public Question(@JsonProperty(value = "online_hearing_id") String onlineHearingId,
                    @JsonProperty(value = "question_id")String questionId,
                    @JsonProperty(value = "question_header_text") String questionHeaderText,
                    @JsonProperty(value = "question_body_text") String questionBodyText) {
        this.onlineHearingId = onlineHearingId;
        this.questionId = questionId;
        this.questionHeaderText = questionHeaderText;
        this.questionBodyText = questionBodyText;
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
}
