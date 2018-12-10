package uk.gov.hmcts.reform.sscscorbackend.service;

import feign.FeignException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;

@Service
public class CohService {

    private final CohClient cohClient;
    private final IdamService idamService;

    public CohService(@Autowired IdamService idamService, @Autowired CohClient cohClient) {
        this.idamService = idamService;
        this.cohClient = cohClient;
    }

    public CohQuestion getQuestion(String onlineHearingId, String questionId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getQuestion(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId, questionId);
    }

    public List<CohAnswer> getAnswers(String onlineHearingId, String questionId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getAnswers(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId, questionId);
    }

    public void createAnswer(String onlineHearingId, String questionId, CohUpdateAnswer newAnswer) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        cohClient.createAnswer(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId, questionId, newAnswer);
    }

    public void updateAnswer(String onlineHearingId, String questionId, String answerId, CohUpdateAnswer newAnswer) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        cohClient.updateAnswer(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId, questionId, answerId, newAnswer);
    }

    public CohQuestionRounds getQuestionRounds(String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getQuestionRounds(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId);
    }

    public String createOnlineHearing(CreateOnlineHearingRequest createOnlineHearingRequest) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.createOnlineHearing(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), createOnlineHearingRequest);
    }

    public CohOnlineHearings getOnlineHearing(Long caseId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getOnlineHearing(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), caseId);
    }

    public CohOnlineHearing getOnlineHearing(String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getOnlineHearing(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId);
    }

    // Empty body sets Content-Length: 0 header which we need to get through our proxy. Cannot get feigns @Headers
    // annotation to work.
    public boolean extendQuestionRoundDeadline(String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        try {
            cohClient.extendQuestionRoundDeadline(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId, "{}");
            return true;
        } catch (FeignException exc) {
            if (exc.status() == HttpStatus.FAILED_DEPENDENCY.value()) {
                return false;
            }
            throw exc;
        }
    }

    public Optional<CohDecision> getDecision(String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getDecision(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId);
    }

    public void addDecisionReply(String onlineHearingId, CohDecisionReply decisionReply) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        cohClient.addDecisionReply(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId, decisionReply);
    }

    public Optional<CohDecisionReplies> getDecisionReplies(String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getDecisionReplies(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId);
    }

    public CohConversations getConversations(String onlineHearingId) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        return cohClient.getConversations(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(), onlineHearingId);
    }
}
