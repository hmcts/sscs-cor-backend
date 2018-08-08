package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

@Service
public class QuestionService {
    private final CohClient cohClient;

    public QuestionService(@Autowired CohClient cohClient) {
        this.cohClient = cohClient;
    }

    public Question getQuestion(String onlineHearingId, String questionId) {
        return cohClient.getQuestion(onlineHearingId, questionId);
    }
}
