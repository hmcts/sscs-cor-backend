package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;

@Service
public class CohService {

    private final String oauthToken = "oauth2Token";
    private final AuthTokenGenerator authTokenGenerator;
    private final CohClient cohClient;

    public CohService(@Autowired AuthTokenGenerator authTokenGenerator, @Autowired CohClient cohClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.cohClient = cohClient;
    }

    public CohQuestion getQuestion(String onlineHearingId, String questionId) {
        return cohClient.getQuestion(oauthToken, authTokenGenerator.generate(), onlineHearingId, questionId);
    }

    public List<CohAnswer> getAnswers(String onlineHearingId, String questionId) {
        return cohClient.getAnswers(oauthToken, authTokenGenerator.generate(), onlineHearingId, questionId);
    }

    public void createAnswer(String onlineHearingId, String questionId, CohUpdateAnswer newAnswer) {
        cohClient.createAnswer(oauthToken, authTokenGenerator.generate(), onlineHearingId, questionId, newAnswer);
    }

    public void updateAnswer(String onlineHearingId, String questionId, String answerId, CohUpdateAnswer newAnswer) {
        cohClient.updateAnswer(oauthToken, authTokenGenerator.generate(), onlineHearingId, questionId, answerId, newAnswer);
    }

    public CohQuestionRounds getQuestionRounds(String onlineHearingId) {
        return cohClient.getQuestionRounds(oauthToken, authTokenGenerator.generate(), onlineHearingId);
    }

    public String createOnlineHearing(CreateOnlineHearingRequest createOnlineHearingRequest) {
        return cohClient.createOnlineHearing(oauthToken, authTokenGenerator.generate(), createOnlineHearingRequest);
    }

    public CohOnlineHearings getOnlineHearing(Long caseId) {
        return cohClient.getOnlineHearing(oauthToken, authTokenGenerator.generate(), caseId);
    }
}
