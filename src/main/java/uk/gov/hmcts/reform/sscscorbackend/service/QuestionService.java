package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

@Service
public class QuestionService {
    @Autowired
    private CohClient cohClient;
    public Question getQuestion(String onlineHearingId, String questionId) {
        return cohClient.getQuestion(onlineHearingId, questionId);
    }
}
