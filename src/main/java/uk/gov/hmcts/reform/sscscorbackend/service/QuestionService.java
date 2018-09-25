package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

@Service
public class QuestionService {
    private final CohService cohService;

    public QuestionService(@Autowired CohService cohService) {
        this.cohService = cohService;
    }

    public Question getQuestion(String onlineHearingId, String questionId) {
        CohQuestion question = cohService.getQuestion(onlineHearingId, questionId);
        if (question != null) {
            List<CohAnswer> answers = cohService.getAnswers(onlineHearingId, questionId);
            if (answers != null && !answers.isEmpty()) {
                return new Question(question.getOnlineHearingId(),
                        question.getQuestionId(),
                        question.getQuestionHeaderText(),
                        question.getQuestionBodyText(),
                        answers.get(0).getAnswerText(),
                        getAnswerState(answers),
                        answers.get(0).getCurrentAnswerState().getStateDateTime());
            } else {
                return new Question(question.getOnlineHearingId(),
                        question.getQuestionId(),
                        question.getQuestionHeaderText(),
                        question.getQuestionBodyText());
            }
        }
        return null;
    }

    public void updateAnswer(String onlineHearingId, String questionId, String newAnswer) {
        List<CohAnswer> answers = cohService.getAnswers(onlineHearingId, questionId);
        CohUpdateAnswer updatedAnswer = new CohUpdateAnswer(AnswerState.draft.getCohAnswerState(), newAnswer);
        if (answers == null || answers.isEmpty()) {
            cohService.createAnswer(onlineHearingId, questionId, updatedAnswer);
        } else {
            String answerId = answers.get(0).getAnswerId();
            cohService.updateAnswer(onlineHearingId, questionId, answerId, updatedAnswer);
        }
    }

    public boolean submitAnswer(String onlineHearingId, String questionId) {
        List<CohAnswer> answers = cohService.getAnswers(onlineHearingId, questionId);

        return answers.stream().findFirst()
                .map(answer -> {
                    CohUpdateAnswer updatedAnswer = new CohUpdateAnswer(AnswerState.submitted.getCohAnswerState(), answer.getAnswerText());
                    String answerId = answers.get(0).getAnswerId();
                    cohService.updateAnswer(onlineHearingId, questionId, answerId, updatedAnswer);

                    return true;
                })
                .orElse(false);
    }

    public QuestionRound getQuestions(String onlineHearingId) {
        CohQuestionRounds questionRounds = cohService.getQuestionRounds(onlineHearingId);

        int currentQuestionRoundNumber = questionRounds.getCurrentQuestionRound();
        CohQuestionRound currentQuestionRound = questionRounds.getCohQuestionRound().get(currentQuestionRoundNumber - 1);
        String deadlineExpiryDate = getQuestionRoundDeadlineExpiryDate(currentQuestionRound);
        List<QuestionSummary> questions = currentQuestionRound.getQuestionReferences().stream()
                .sorted(Comparator.comparing(CohQuestionReference::getQuestionOrdinal))
                .map(this::createQuestionSummary)
                .collect(toList());

        return new QuestionRound(questions, deadlineExpiryDate);
    }

    public QuestionRound extendQuestionRoundDeadline(String onlineHearingId) {
        cohService.extendQuestionRoundDeadline(onlineHearingId);

        return getQuestions(onlineHearingId);
    }

    private String getQuestionRoundDeadlineExpiryDate(CohQuestionRound questionRound) {
        List<CohQuestionReference> questionRefsForRound = questionRound.getQuestionReferences();
        if (questionRefsForRound != null && !questionRefsForRound.isEmpty()) {
            return questionRound.getQuestionReferences().get(0).getDeadlineExpiryDate();
        } else {
            throw new IllegalStateException(
                "Cannot get questions required by date as question round has been published with no questions in it"
            );
        }
    }

    private QuestionSummary createQuestionSummary(CohQuestionReference cohQuestionReference) {
        List<CohAnswer> answers = cohQuestionReference.getAnswers();

        AnswerState answerState = getAnswerState(answers);

        return new QuestionSummary(
                cohQuestionReference.getQuestionId(),
                cohQuestionReference.getQuestionHeaderText(),
                answerState
        );
    }

    private AnswerState getAnswerState(List<CohAnswer> answers) {
        return ofNullable(answers).orElse(emptyList()).stream()
                    .findFirst()
                    .map(cohAnswer -> cohAnswer.getCurrentAnswerState().getStateName())
                    .map(AnswerState::of)
                    .orElse(AnswerState.unanswered);
    }
}
