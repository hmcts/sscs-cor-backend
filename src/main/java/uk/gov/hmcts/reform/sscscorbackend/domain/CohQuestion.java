package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohQuestion {

    private final String onlineHearingId;
    private final String questionId;
    private final int questionOrdinal;
    private final String questionHeaderText;
    private final String questionBodyText;

    public CohQuestion(@JsonProperty(value = "online_hearing_id") String onlineHearingId,
                       @JsonProperty(value = "question_id")String questionId,
                       @JsonProperty(value = "question_ordinal") int questionOrdinal,
                       @JsonProperty(value = "question_header_text") String questionHeaderText,
                       @JsonProperty(value = "question_body_text") String questionBodyText) {
        this.onlineHearingId = onlineHearingId;
        this.questionId = questionId;
        this.questionOrdinal = questionOrdinal;
        this.questionHeaderText = questionHeaderText;
        this.questionBodyText = questionBodyText;
    }

    public String getOnlineHearingId() {
        return onlineHearingId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public int getQuestionOrdinal() {
        return questionOrdinal;
    }

    public String getQuestionHeaderText() {
        return questionHeaderText;
    }

    public String getQuestionBodyText() {
        return questionBodyText;
    }
}
