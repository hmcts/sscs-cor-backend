package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.sscscorbackend.domain.AnswerState.unanswered;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

@Service
public class QuestionService {
    private final CohService cohService;
    private final EvidenceUploadService evidenceUploadService;

    public QuestionService(@Autowired CohService cohService, @Autowired EvidenceUploadService evidenceUploadService) {
        this.cohService = cohService;
        this.evidenceUploadService = evidenceUploadService;
    }

    public Question getQuestion(String onlineHearingId, String questionId) {
        CohQuestion question = cohService.getQuestion(onlineHearingId, questionId);
        if (question != null) {
            List<CohAnswer> answers = cohService.getAnswers(onlineHearingId, questionId);
            List<Evidence> evidence = evidenceUploadService.listEvidence(onlineHearingId, questionId);
            if (answers != null && !answers.isEmpty()) {
                return new Question(question.getOnlineHearingId(),
                        question.getQuestionId(),
                        question.getQuestionOrdinal(),
                        question.getQuestionHeaderText(),
                        question.getQuestionBodyText(),
                        answers.get(0).getAnswerText(),
                        updateAnswerStateIfEvidencePresent(getAnswerState(answers), evidence),
                        answers.get(0).getCurrentAnswerState().getStateDateTime(),
                        evidence
                );
            } else {
                return new Question(question.getOnlineHearingId(),
                        question.getQuestionId(),
                        question.getQuestionOrdinal(),
                        question.getQuestionHeaderText(),
                        question.getQuestionBodyText(),
                        null,
                        updateAnswerStateIfEvidencePresent(AnswerState.unanswered, evidence),
                        null,
                        evidence);
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
        Map<String, List<Evidence>> evidencePerQuestion = evidenceUploadService.listEvidence(onlineHearingId);

        int currentQuestionRoundNumber = questionRounds.getCurrentQuestionRound();
        CohQuestionRound currentQuestionRound = questionRounds.getCohQuestionRound().get(currentQuestionRoundNumber - 1);
        String deadlineExpiryDate = getQuestionRoundDeadlineExpiryDate(currentQuestionRound);
        int deadlineExtensionCount = currentQuestionRound.getDeadlineExtensionCount();
        List<QuestionSummary> questions = currentQuestionRound.getQuestionReferences().stream()
                .sorted(Comparator.comparing(CohQuestionReference::getQuestionOrdinal))
                .map(cohQuestionReference ->
                        createQuestionSummary(cohQuestionReference, evidencePerQuestion.getOrDefault(cohQuestionReference.getQuestionId(), emptyList())))
                .collect(toList());

        return new QuestionRound(questions, deadlineExpiryDate, deadlineExtensionCount);
    }

    public Optional<QuestionRound> extendQuestionRoundDeadline(String onlineHearingId) {
        boolean haveExtendedRound = cohService.extendQuestionRoundDeadline(onlineHearingId);
        if (haveExtendedRound) {
            return Optional.of(getQuestions(onlineHearingId));
        } else {
            return Optional.empty();
        }
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

    private QuestionSummary createQuestionSummary(CohQuestionReference cohQuestionReference, List<Evidence> evidence) {
        List<CohAnswer> answers = cohQuestionReference.getAnswers();

        AnswerState answerState = updateAnswerStateIfEvidencePresent(getAnswerState(answers), evidence);

        return new QuestionSummary(
                cohQuestionReference.getQuestionId(),
                cohQuestionReference.getQuestionOrdinal(),
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

    private AnswerState updateAnswerStateIfEvidencePresent(AnswerState answerState, List<Evidence> evidenceList) {
        if (unanswered.equals(answerState) && !evidenceList.isEmpty()) {
            return AnswerState.draft;
        }
        return answerState;
    }
}
