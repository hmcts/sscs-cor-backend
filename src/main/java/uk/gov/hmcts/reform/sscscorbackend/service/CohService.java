package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;

@Service
public class CohService {

    private static final String OAUTH2_TOKEN = "oauth2Token";
    private final AuthTokenGenerator authTokenGenerator;
    private final CohClient cohClient;

    public CohService(@Autowired AuthTokenGenerator authTokenGenerator, @Autowired CohClient cohClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.cohClient = cohClient;
    }

    public CohQuestion getQuestion(String onlineHearingId, String questionId) {
        return cohClient.getQuestion(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId, questionId);
    }

    public List<CohAnswer> getAnswers(String onlineHearingId, String questionId) {
        return cohClient.getAnswers(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId, questionId);
    }

    public void createAnswer(String onlineHearingId, String questionId, CohUpdateAnswer newAnswer) {
        cohClient.createAnswer(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId, questionId, newAnswer);
    }

    public void updateAnswer(String onlineHearingId, String questionId, String answerId, CohUpdateAnswer newAnswer) {
        cohClient.updateAnswer(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId, questionId, answerId, newAnswer);
    }

    public CohQuestionRounds getQuestionRounds(String onlineHearingId) {
        return cohClient.getQuestionRounds(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId);
    }

    public String createOnlineHearing(CreateOnlineHearingRequest createOnlineHearingRequest) {
        return cohClient.createOnlineHearing(OAUTH2_TOKEN, authTokenGenerator.generate(), createOnlineHearingRequest);
    }

    public CohOnlineHearings getOnlineHearing(Long caseId) {
        return cohClient.getOnlineHearing(OAUTH2_TOKEN, authTokenGenerator.generate(), caseId);
    }

    // Empty body sets Content-Length: 0 header which we need to get through our proxy. Cannot get feigns @Headers
    // annotation to work.
    public void extendQuestionRoundDeadline(String onlineHearingId) {
        cohClient.extendQuestionRoundDeadline(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId, "{}");
    }

    public Optional<CohDecision> getDecision(String onlineHearingId) {
        return cohClient.getDecision(OAUTH2_TOKEN, authTokenGenerator.generate(), onlineHearingId);
    }
}
