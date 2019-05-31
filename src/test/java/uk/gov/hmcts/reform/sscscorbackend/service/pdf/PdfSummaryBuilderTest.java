package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.somePdfAppealDetails;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfQuestion;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfQuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.domain.pdf.PdfSummary;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.*;

public class PdfSummaryBuilderTest {
    @Test
    public void buildPdfQuestions() {
        String round1IssuedDate = "2018-06-05T13:53:42Z";
        String round2IssuedDate = "2018-12-11T10:00:01Z";

        List<CohState> historyRound1 = singletonList(new CohState("question_issued", round1IssuedDate));
        List<CohState> historyRound2 = singletonList(new CohState("question_issued", round2IssuedDate));

        String answer1AnsweredDate = "2018-07-05T13:53:42Z";
        String answer2AnsweredDate = "2018-12-12T10:00:01Z";

        List<CohState> answerHistoryRound1 = singletonList(new CohState("answer_submitted", answer1AnsweredDate));
        List<CohState> answerHistoryRound2 = singletonList(new CohState("answer_submitted", answer2AnsweredDate));

        CohConversations cohConversations = new CohConversations(
                new CohConversation(asList(
                        createCohQuestion(2, 2, historyRound2, answerHistoryRound2),
                        createCohQuestion(1, 1, historyRound1, answerHistoryRound1),
                        createCohQuestion(2, 1, historyRound2, answerHistoryRound2),
                        createCohQuestion(1, 2, historyRound1, answerHistoryRound1)
                ),
                new CohRelisting("Relisting reason")
        ));

        List<PdfQuestionRound> pdfQuestionRounds = new PdfSummaryBuilder().buildPdfSummary(cohConversations, somePdfAppealDetails()).getQuestionRounds();

        assertThat(pdfQuestionRounds, is(asList(
                new PdfQuestionRound(asList(
                        new PdfQuestion("questionHeader-1-1", "questionBody-1-1", "answerText-1-1", AnswerState.submitted, "5 June 2018", "5 July 2018"),
                        new PdfQuestion("questionHeader-1-2", "questionBody-1-2", "answerText-1-2", AnswerState.submitted, "5 June 2018", "5 July 2018")
                )),
                new PdfQuestionRound(asList(
                        new PdfQuestion("questionHeader-2-1", "questionBody-2-1", "answerText-2-1", AnswerState.submitted, "11 December 2018", "12 December 2018"),
                        new PdfQuestion("questionHeader-2-2", "questionBody-2-2", "answerText-2-2", AnswerState.submitted, "11 December 2018", "12 December 2018")
                ))
        )));
    }

    @Test
    public void buildPdfQuestionsWhereRoundHasNotBeenIssued() {
        List<CohState> historyRound1 = emptyList();
        CohConversations cohConversations = new CohConversations(new CohConversation(
                singletonList(createCohQuestionWithAnswer(1, 1, historyRound1, null)),
                new CohRelisting("Relisting reason")));

        List<PdfQuestionRound> pdfQuestionRounds = new PdfSummaryBuilder().buildPdfSummary(cohConversations, somePdfAppealDetails()).getQuestionRounds();

        assertThat(pdfQuestionRounds, is(singletonList(
                new PdfQuestionRound(singletonList(
                        new PdfQuestion("questionHeader-1-1", "questionBody-1-1", "", AnswerState.unanswered, "", "")
                )))));
    }

    @Test
    public void buildPdfQuestionsWithNoAnswer() {
        String round1IssuedDate = "2018-06-05T13:53:42Z";

        List<CohState> historyRound1 = singletonList(new CohState("question_issued", round1IssuedDate));

        CohConversations cohConversations = new CohConversations(new CohConversation(
                singletonList(createCohQuestionWithAnswer(1, 1, historyRound1, null)),
                new CohRelisting("Relisting reason")));

        List<PdfQuestionRound> pdfQuestionRounds = new PdfSummaryBuilder().buildPdfSummary(cohConversations, somePdfAppealDetails()).getQuestionRounds();

        assertThat(pdfQuestionRounds, is(singletonList(
                new PdfQuestionRound(singletonList(
                        new PdfQuestion("questionHeader-1-1", "questionBody-1-1", "", AnswerState.unanswered, "5 June 2018", "")
                )))));
    }

    @Test
    public void buildPdfSummaryWithRelistingReason() {
        String relistingReason = "Relisting reason";
        PdfSummary pdfSummary = new PdfSummaryBuilder().buildPdfSummary(new CohConversations(
                new CohConversation(Collections.emptyList(), new CohRelisting(relistingReason))),
                somePdfAppealDetails()
        );

        assertThat(pdfSummary.getRelistingReason(), is(relistingReason));
    }

    @Test
    public void buildPdfSummaryWithNullRelisting() {
        PdfSummary pdfSummary = new PdfSummaryBuilder().buildPdfSummary(new CohConversations(
                        new CohConversation(Collections.emptyList(), null)),
                somePdfAppealDetails()
        );

        assertThat(pdfSummary.getRelistingReason(), is(""));
    }

    @Test
    public void buildPdfSummaryWithMultiLineRelistingReson() {
        PdfSummary pdfSummary = new PdfSummaryBuilder().buildPdfSummary(new CohConversations(
                        new CohConversation(Collections.emptyList(), new CohRelisting("this \\n has multi\\nlines"))),
                somePdfAppealDetails()
        );

        assertThat(pdfSummary.getRelistingReason(), is("this \n has multi\nlines"));
    }

    private CohQuestion createCohQuestion(int roundNumber, int questionOrdinal, List<CohState> historyRound, List<CohState> historyAnswer) {
        String suffix = roundNumber + "-" + questionOrdinal;
        return createCohQuestionWithAnswer(roundNumber, questionOrdinal, historyRound, singletonList(new CohAnswer(
                "answerId-" + suffix,
                "answerText-" + suffix,
                new CohState("answer_submitted", ""),
                historyAnswer)
        ));
    }

    private CohQuestion createCohQuestionWithAnswer(int roundNumber, int questionOrdinal, List<CohState> historyRound, List<CohAnswer> answers) {
        String suffix = roundNumber + "-" + questionOrdinal;
        return new CohQuestion(
                "onlineHearingId",
                roundNumber,
                "questionId-" + suffix,
                questionOrdinal,
                "questionHeader-" + suffix,
                "questionBody-" + suffix,
                historyRound,
                answers
        );
    }

}