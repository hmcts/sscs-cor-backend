package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohAnswer;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohConversations;
import uk.gov.hmcts.reform.sscscorbackend.domain.CohQuestion;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfAppealDetails;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfQuestion;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfQuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;

@Service
public class PdfSummaryBuilder {
    public PdfSummary buildPdfSummary(CohConversations conversations, PdfAppealDetails appealDetails) {
        return new PdfSummary(
                appealDetails,
                buildQuestionRounds(conversations)
        );
    }

    private List<PdfQuestionRound> buildQuestionRounds(CohConversations conversations) {
        Map<Integer, List<CohQuestion>> questionsByRound = conversations.getConversation().getQuestions().stream()
                .collect(byRoundId());

        return questionsByRound.entrySet().stream()
                .sorted(byRoundNumber())
                .map(toPdfQuestions())
                .map(PdfQuestionRound::new)
                .collect(toList());
    }

    private Collector<CohQuestion, ?, Map<Integer, List<CohQuestion>>> byRoundId() {
        return groupingBy(CohQuestion::getQuestionRound);
    }

    private Comparator<Map.Entry<Integer, List<CohQuestion>>> byRoundNumber() {
        return comparing(Map.Entry::getKey);
    }

    private Comparator<CohQuestion> byQuestionOrdinal() {
        return comparingInt(CohQuestion::getQuestionOrdinal);
    }

    private Function<Map.Entry<Integer, List<CohQuestion>>, List<PdfQuestion>> toPdfQuestions() {
        return round -> round.getValue().stream()
                .sorted(byQuestionOrdinal())
                .map(createPdfQuestion())
                .collect(toList());
    }

    private Function<CohQuestion, PdfQuestion> createPdfQuestion() {
        return question -> new PdfQuestion(
                question.getQuestionHeaderText(),
                question.getQuestionBodyText(),
                getAnswer(question),
                formatDate(question.getIssueDate()),
                formatDate(question.getSubmittedDate())
        );
    }

    private String getAnswer(CohQuestion question) {
        return question.getAnswer()
                .map(CohAnswer::getAnswerText)
                .orElse("");
    }

    private String formatDate(Optional<String> issueDate) {
        return issueDate
                .map(cohIssueDate -> parse(cohIssueDate, ISO_DATE_TIME))
                .map(issueDateLocalDate -> issueDateLocalDate.format(ofPattern("d MMMM yyyy")))
                .orElse("");
    }
}
