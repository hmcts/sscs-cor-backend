package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscscorbackend.DataFixtures.*;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.evidence.EvidenceUploadService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.CohService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.api.*;

public class QuestionServiceTest {
    private CohService cohService;
    private CohQuestion cohQuestion;
    private String onlineHearingId;
    private String questionId;
    private CohAnswer cohAnswer;
    private QuestionService underTest;
    private EvidenceUploadService evidenceUploadService;

    @Before
    public void setUp() {
        cohService = mock(CohService.class);
        cohQuestion = someCohQuestion();
        onlineHearingId = cohQuestion.getOnlineHearingId();
        questionId = cohQuestion.getQuestionId();
        cohAnswer = someCohAnswer();
        evidenceUploadService = mock(EvidenceUploadService.class);
        underTest = new QuestionService(cohService, evidenceUploadService);
    }

    @Test
    public void getsAnEmptyListOfQuestionsWhenNoRoundsHaveBeenIssued() {
        CohQuestionRounds cohQuestionRounds = someUnpublishedCohQuestionRounds();

        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, is(emptyList()));
    }

    @Test
    public void getsAListOfQuestionsWhenThereIsOnlyOneRoundOfQuestions() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();
        CohQuestionReference cohQuestionReference1 = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        CohQuestionReference cohQuestionReference2 = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        QuestionSummary question1Summary = new QuestionSummary(cohQuestionReference1.getQuestionId(),
                cohQuestionReference1.getQuestionOrdinal(),
                cohQuestionReference1.getQuestionHeaderText(),
                cohQuestionReference1.getQuestionBodyText(),
                draft,
                null,
                cohQuestionReference1.getAnswers().get(0).getAnswerText());
        QuestionSummary question2Summary = new QuestionSummary(cohQuestionReference2.getQuestionId(),
                cohQuestionReference2.getQuestionOrdinal(),
                cohQuestionReference2.getQuestionHeaderText(),
                cohQuestionReference2.getQuestionBodyText(),
                draft,
                null,
                cohQuestionReference2.getAnswers().get(0).getAnswerText());
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions.get(0), is(question1Summary));
        assertThat(questions.get(1), is(question2Summary));
    }

    @Test
    public void getsAListOfPendingQuestions() {
        List<CohQuestionReference> cohQuestionReferenceList = Arrays.asList(
                new CohQuestionReference("someQuestionId1", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))
        );
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(cohQuestionReferenceList, 0, someCohState("question_issue_pending"))));
        CohQuestionReference cohQuestionReference1 = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        QuestionSummary question1Summary = new QuestionSummary(cohQuestionReference1.getQuestionId(),
                cohQuestionReference1.getQuestionOrdinal(),
                cohQuestionReference1.getQuestionHeaderText(),
                cohQuestionReference1.getQuestionBodyText(),
                draft,
                null,
                cohQuestionReference1.getAnswers().get(0).getAnswerText());

        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, false);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions.get(0), is(question1Summary));
    }

    @Test
    public void doesNotGetsAListOfPendingQuestions() {
        List<CohQuestionReference> cohQuestionReferenceList = Arrays.asList(
                new CohQuestionReference("someQuestionId1", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))
        );
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(cohQuestionReferenceList, 0, someCohState("question_issue_pending"))));

        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions.size(), is(0));
    }

    @Test
    public void getsAListOfQuestionsWithDeadlineExpiryDateFromFirstQuestion() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();
        CohQuestionReference cohQuestion1Reference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        CohQuestionReference cohQuestion2Reference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);

        assertThat(questionRound.getDeadlineExpiryDate(), is(cohQuestion1Reference.getDeadlineExpiryDate()));
        assertThat(questionRound.getDeadlineExpiryDate(), not(cohQuestion2Reference.getDeadlineExpiryDate()));
    }

    @Test
    public void getsAListOfQuestionsWithUnansweredStatesWhenTheQuestionHasNotBeenAnswered() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), null)
                ), 0, someCohState("question_issued"))
        ));
        QuestionSummary questionSummary = createQuestionSummary(cohQuestionRounds, 0, unanswered, "");

        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);

        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsWithNoAnswerButHasEvidence() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(
                new CohQuestionRound(singletonList(
                        new CohQuestionReference("someQuestionId", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), null)
                ), 0, someCohState("question_issued"))
        ));

        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        HashMap<String, List<Evidence>> evidenceToQuestionsMap = new HashMap<>();
        evidenceToQuestionsMap.put(questionId, asList(someEvidence()));
        when(evidenceUploadService.listQuestionEvidence(onlineHearingId)).thenReturn(evidenceToQuestionsMap);

        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        QuestionSummary questionSummary = createQuestionSummary(cohQuestionRounds, 0, draft, "");
        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsWhenThereIsMultipleRoundsOfQuestions() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsMultipleRoundsOfQuestions();
        QuestionSummary questionSummary = createQuestionSummary(cohQuestionRounds, 1, draft, "");
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(questionSummary));
    }

    @Test
    public void getsAListOfQuestionsInTheCorrectOrderWhenTheyAreReturnedInTheIncorrectOrder() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference("questionId2", 2, "second question", "second question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted")),
                        new CohQuestionReference("questionId1", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), someCohAnswers("answer_drafted"))), 0, someCohState("question_issued"))
        ));
        CohQuestionReference firstCohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(1);
        String firstQuestionId = firstCohQuestionReference.getQuestionId();
        int firstQuestionOrdinal = firstCohQuestionReference.getQuestionOrdinal();
        String firstQuestionTitle = firstCohQuestionReference.getQuestionHeaderText();
        String firstQuestionBody = firstCohQuestionReference.getQuestionBodyText();
        String firstAnswerText = firstCohQuestionReference.getAnswers().get(0).getAnswerText();
        CohQuestionReference secondCohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(0)
                .getQuestionReferences().get(0);
        String secondQuestionId = secondCohQuestionReference.getQuestionId();
        int secondQuestionOrdinal = secondCohQuestionReference.getQuestionOrdinal();
        String secondQuestionTitle = secondCohQuestionReference.getQuestionHeaderText();
        String secondQuestionBody = secondCohQuestionReference.getQuestionBodyText();
        String secondAnswerText = secondCohQuestionReference.getAnswers().get(0).getAnswerText();
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);
        List<QuestionSummary> questions = questionRound.getQuestions();

        assertThat(questions, contains(
                new QuestionSummary(firstQuestionId, firstQuestionOrdinal, firstQuestionTitle, firstQuestionBody, draft, null, firstAnswerText),
                new QuestionSummary(secondQuestionId, secondQuestionOrdinal, secondQuestionTitle, secondQuestionBody, draft, null, secondAnswerText)
                )
        );
    }

    @Test
    public void getsAnsweredDateFromAnsweredQuestion() {
        String expectedAnsweredDate = "2018-08-08T09:12:12Z";
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference("questionId1", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), singletonList(new CohAnswer("", "", new CohState("answer_submitted", expectedAnsweredDate), singletonList(new CohState("answer_submitted", expectedAnsweredDate)))))), 0, someCohState("question_issued"))
        ));
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);

        assertThat(questionRound.getQuestions().size(), is(1));
        assertThat(questionRound.getQuestions().get(0).getAnsweredDate(), is(expectedAnsweredDate));
    }

    @Test
    public void getsNullAnsweredDateFromUnansweredQuestion() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference("questionId1", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), singletonList(new CohAnswer("", "", new CohState("answer_drafted", "2018-08-08T09:12:12Z"), singletonList(new CohState("answer_drafted", "2018-08-08T09:12:12Z")))))), 0, someCohState("question_issued"))
        ));
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);

        assertThat(questionRound.getQuestions().size(), is(1));
        assertThat(questionRound.getQuestions().get(0).getAnsweredDate(), is(nullValue()));
    }

    @Test
    public void getsAnEmptyListOfQuestionsIfTheCurrentRoundHasNotBeenIssued() {
        CohQuestionRounds cohQuestionRounds = new CohQuestionRounds(1, singletonList(new CohQuestionRound(
                asList(new CohQuestionReference("questionId1", 1, "first question", "first question body", now().plusDays(7).format(ISO_LOCAL_DATE_TIME), null)), 0, someCohState("question_drafted"))
        ));
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);
        QuestionRound questionRound = underTest.getQuestions(onlineHearingId, true);

        assertThat(questionRound.getQuestions(), is(emptyList()));
    }

    @Test
    public void getsAQuestionWithAnAnswerAndEvidence() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(singletonList(cohAnswer));
        List<Evidence> evidenceList = singletonList(someEvidence());
        when(evidenceUploadService.listQuestionEvidence(onlineHearingId, questionId)).thenReturn(evidenceList);

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(new Question(cohQuestion.getOnlineHearingId(),
                cohQuestion.getQuestionId(),
                cohQuestion.getQuestionOrdinal(),
                cohQuestion.getQuestionHeaderText(),
                cohQuestion.getQuestionBodyText(),
                cohAnswer.getAnswerId(),
                cohAnswer.getAnswerText(),
                AnswerState.of(cohAnswer.getCurrentAnswerState().getStateName()),
                cohAnswer.getCurrentAnswerState().getStateDateTime(),
                evidenceList))
        );
    }

    @Test
    public void getsAQuestionWithoutAnAnswer() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());
        when(evidenceUploadService.listQuestionEvidence(onlineHearingId, questionId)).thenReturn(emptyList());

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(new Question(cohQuestion.getOnlineHearingId(),
                cohQuestion.getQuestionId(),
                cohQuestion.getQuestionOrdinal(),
                cohQuestion.getQuestionHeaderText(),
                cohQuestion.getQuestionBodyText(),
                null,
                null,
                unanswered,
                null,
                emptyList()))
        );
    }

    @Test
    public void getsAQuestionWithoutAnAnswerAndEvidence() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(cohQuestion);
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());
        List<Evidence> evidenceList = singletonList(someEvidence());
        when(evidenceUploadService.listQuestionEvidence(onlineHearingId, questionId)).thenReturn(evidenceList);

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(new Question(cohQuestion.getOnlineHearingId(),
                cohQuestion.getQuestionId(),
                cohQuestion.getQuestionOrdinal(),
                cohQuestion.getQuestionHeaderText(),
                cohQuestion.getQuestionBodyText(),
                null,
                null,
                draft,
                null,
                evidenceList))
        );
    }

    @Test
    public void doesNotFindQuestion() {
        when(cohService.getQuestion(onlineHearingId, questionId)).thenReturn(null);

        Question question = underTest.getQuestion(onlineHearingId, questionId);

        assertThat(question, is(nullValue()));
        verify(cohService, never()).getAnswers(onlineHearingId, questionId);
    }

    @Test
    public void updateAnswerWhenQuestionHasNotAlreadyBeenAnswered() {
        String newAnswer = "new answer";
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());
        underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        verify(cohService).createAnswer(onlineHearingId, questionId, new CohUpdateAnswer("answer_drafted", newAnswer));
    }

    @Test
    public void updateAnswerWhenQuestionHasAlreadyBeenAnswered() {
        String newAnswer = "new answer";
        String answerId = "some-id";
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(
                singletonList(new CohAnswer(answerId, "original answer", someCohState("answer_draQuestionService.javafted"), emptyList()))
        );
        underTest.updateAnswer(onlineHearingId, questionId, newAnswer);

        verify(cohService).updateAnswer(onlineHearingId, questionId, answerId, new CohUpdateAnswer("answer_drafted", newAnswer));
    }

    @Test
    public void submitAnswer() {
        String answerId = "some-id";
        String answer = "answer";

        final CohAnswer answer_drafted = new CohAnswer(answerId, answer, someCohState("answer_drafted"), emptyList());
        String questionHeader = "question header";
        when(cohService.getQuestion(onlineHearingId, questionId))
                .thenReturn(createCohQuestion(questionHeader, singletonList(answer_drafted)));
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(
                singletonList(new CohAnswer(answerId, answer, someCohState("answer_drafted"), emptyList()))
        );

        boolean hasBeenSubmitted = underTest.submitAnswer(onlineHearingId, questionId);

        assertThat(hasBeenSubmitted, is(true));
        verify(cohService).updateAnswer(onlineHearingId, questionId, answerId, new CohUpdateAnswer(submitted.getCohAnswerState(), answer));
        verify(evidenceUploadService).submitQuestionEvidence(eq(onlineHearingId), argThat(argument -> questionId.equals(argument.getQuestionId())));
    }

    @Test
    public void cannotSubmitAnswerThatHasNotAlreadyBeenAnswered() {
        when(cohService.getQuestion(onlineHearingId, questionId))
                .thenReturn(createCohQuestion("question header", emptyList()));
        when(cohService.getAnswers(onlineHearingId, questionId)).thenReturn(emptyList());

        boolean hasBeenSubmitted = underTest.submitAnswer(onlineHearingId, questionId);

        assertThat(hasBeenSubmitted, is(false));
        verify(cohService, never()).updateAnswer(any(), any(), any(), any());
        verify(evidenceUploadService, never()).submitQuestionEvidence(any(), any());
    }

    @Test
    public void extendsQuestionRoundDeadLine() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();
        when(cohService.extendQuestionRoundDeadline(onlineHearingId)).thenReturn(true);
        when(cohService.getQuestionRounds(onlineHearingId)).thenReturn(cohQuestionRounds);

        Optional<QuestionRound> extendedQuestionRound = underTest.extendQuestionRoundDeadline(onlineHearingId);

        String deadlineExpiryDate = cohQuestionRounds.getCohQuestionRound().get(0).getQuestionReferences()
                .get(0).getDeadlineExpiryDate();

        assertThat(extendedQuestionRound.isPresent(), is(true));
        assertThat(extendedQuestionRound.get().getDeadlineExpiryDate(), is(deadlineExpiryDate));
    }

    @Test
    public void cannotExtendQuestionRoundDeadLine() {
        when(cohService.extendQuestionRoundDeadline(onlineHearingId)).thenReturn(false);

        Optional<QuestionRound> extendedQuestionRound = underTest.extendQuestionRoundDeadline(onlineHearingId);

        assertThat(extendedQuestionRound.isPresent(), is(false));
    }

    @Test
    public void checkRoundHasDefaultExtensionCount() {
        CohQuestionRounds cohQuestionRounds = someCohQuestionRoundsWithSingleRoundOfQuestions();

        assertThat(cohQuestionRounds.getCohQuestionRound().get(0).getDeadlineExtensionCount(), is(0));
    }

    private CohQuestion createCohQuestion(String questionHeader, List<CohAnswer> answers) {
        return new CohQuestion(onlineHearingId,
                1,
                questionId,
                1,
                questionHeader,
                "question body",
                emptyList(),
                answers
        );
    }

    private QuestionSummary createQuestionSummary(CohQuestionRounds cohQuestionRounds, int i, AnswerState answerState, String answer) {
        CohQuestionReference cohQuestionReference = cohQuestionRounds.getCohQuestionRound().get(i)
                .getQuestionReferences().get(0);
        String id = cohQuestionReference.getQuestionId();
        int questionOrdinal = cohQuestionReference.getQuestionOrdinal();
        String questionHeaderText = cohQuestionReference.getQuestionHeaderText();
        String questionBodyText = cohQuestionReference.getQuestionBodyText();
        List<CohAnswer> answers = cohQuestionReference.getAnswers();
        String answerText = (answers != null && answers.size() > 0) ? answers.get(0).getAnswerText() : "";
        return new QuestionSummary(id, questionOrdinal, questionHeaderText, questionBodyText, answerState, null, answerText);
    }
}