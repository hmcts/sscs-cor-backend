package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;

@Service
public class QuestionService {
    private final CohClient cohClient;

    public QuestionService(@Autowired CohClient cohClient) {
        this.cohClient = cohClient;
    }

    public Question getQuestion(String onlineHearingId, String questionId) {
        CohQuestion question = cohClient.getQuestion(onlineHearingId, questionId);
        if (question != null) {
            List<CohAnswer> answer = cohClient.getAnswers(onlineHearingId, questionId);
            if (answer != null && answer.size() > 0) {
                return Question.from(question, answer.get(0));
            } else {
                return Question.from(question);
            }
        }

        return null;
    }

    public void updateAnswer(String onlineHearingId, String questionId, String newAnswer) {
        List<CohAnswer> answers = cohClient.getAnswers(onlineHearingId, questionId);
        CohUpdateAnswer updatedAnswer = new CohUpdateAnswer("answer_drafted", newAnswer);
        if (answers == null || answers.isEmpty()) {
            cohClient.createAnswer(onlineHearingId, questionId, updatedAnswer);
        } else {
            String answerId = answers.get(0).getAnswerId();
            cohClient.updateAnswer(onlineHearingId, questionId, answerId, updatedAnswer);
        }
    }

    public QuestionRound getQuestions(String onlineHearingId) {
        CohQuestionRounds questionRounds = cohClient.getQuestionRounds(onlineHearingId);

        int currentQuestionRoundNumber = questionRounds.getCurrentQuestionRound();
        CohQuestionRound currentQuestionRound = questionRounds.getCohQuestionRound().get(currentQuestionRoundNumber - 1);
        List<QuestionSummary> questions = currentQuestionRound.getQuestionReferences().stream()
                .sorted(Comparator.comparing(CohQuestionReference::getQuestionOrdinal))
                .map(cohQuestionReference -> new QuestionSummary(cohQuestionReference.getQuestionHeaderText()))
                .collect(toList());

        return new QuestionRound(questions);
    }
}
