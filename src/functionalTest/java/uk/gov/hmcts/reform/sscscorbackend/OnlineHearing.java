package uk.gov.hmcts.reform.sscscorbackend;

public class OnlineHearing {
    private String emailAddress;
    private String hearingId;
    private String questionId;

    public OnlineHearing(String emailAddress, String hearingId, String questionId) {
        this.emailAddress = emailAddress;
        this.hearingId = hearingId;
        this.questionId = questionId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getHearingId() {
        return hearingId;
    }

    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
}
