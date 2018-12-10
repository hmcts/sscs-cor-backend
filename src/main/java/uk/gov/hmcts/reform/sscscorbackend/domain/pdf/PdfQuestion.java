package uk.gov.hmcts.reform.sscscorbackend.domain.pdf;

import java.util.Objects;

public class PdfQuestion {
    private final String questionTitle;
    private final String questionBody;
    private final String answer;
    private final String issuedDate;
    private final String submittedDate;

    public PdfQuestion(String questionTitle, String questionBody, String answer, String issuedDate, String submittedDate) {
        this.questionTitle = questionTitle;
        this.questionBody = questionBody;
        this.answer = answer;
        this.issuedDate = issuedDate;
        this.submittedDate = submittedDate;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public String getQuestionBody() {
        return questionBody;
    }

    public String getAnswer() {
        return answer;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PdfQuestion that = (PdfQuestion) o;
        return Objects.equals(questionTitle, that.questionTitle) &&
                Objects.equals(questionBody, that.questionBody) &&
                Objects.equals(answer, that.answer) &&
                Objects.equals(issuedDate, that.issuedDate) &&
                Objects.equals(submittedDate, that.submittedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionTitle, questionBody, answer, issuedDate, submittedDate);
    }

    @Override
    public String toString() {
        return "PdfQuestion{" +
                "questionTitle='" + questionTitle + '\'' +
                ", questionBody='" + questionBody + '\'' +
                ", answer='" + answer + '\'' +
                ", issuedDate='" + issuedDate + '\'' +
                ", submittedDate='" + submittedDate + '\'' +
                '}';
    }
}
