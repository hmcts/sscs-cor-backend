package uk.gov.hmcts.reform.sscscorbackend.domain.pdf;

import java.util.List;
import java.util.Objects;

public class PdfSummary {
    private final PdfAppealDetails appealDetails;
    private final List<PdfQuestionRound> questionRounds;

    public PdfSummary(PdfAppealDetails appealDetails, List<PdfQuestionRound> questionRounds) {
        this.appealDetails = appealDetails;
        this.questionRounds = questionRounds;
    }

    public PdfAppealDetails getAppealDetails() {
        return appealDetails;
    }

    public List<PdfQuestionRound> getQuestionRounds() {
        return questionRounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PdfSummary that = (PdfSummary) o;
        return Objects.equals(appealDetails, that.appealDetails) &&
                Objects.equals(questionRounds, that.questionRounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appealDetails, questionRounds);
    }

    @Override
    public String toString() {
        return "PdfSummary{" +
                "appealDetails=" + appealDetails +
                ", questionRounds=" + questionRounds +
                '}';
    }
}
