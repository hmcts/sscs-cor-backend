package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;


@FeignClient(
        name = "Coh",
        url = "${coh.url}",
        decode404 = true
)
public interface CohClient {

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}")
    CohQuestion getQuestion(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @PathVariable("onlineHearingId") String onlineHearingId,
            @PathVariable("questionId") String questionId);

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers")
    List<CohAnswer> getAnswers(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @PathVariable("onlineHearingId") String onlineHearingId,
            @PathVariable("questionId") String questionId);

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers"
    )
    void createAnswer(@RequestHeader(AUTHORIZATION) String authorisation,
                      @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                      @PathVariable("onlineHearingId") String onlineHearingId,
                      @PathVariable("questionId") String questionId,
                      @RequestBody CohUpdateAnswer newAnswer);

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers/{answerId}"
    )
    void updateAnswer(@RequestHeader(AUTHORIZATION) String authorisation,
                      @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                      @PathVariable("onlineHearingId") String onlineHearingId,
                      @PathVariable("questionId") String questionId,
                      @PathVariable("answerId") String answerId,
                      @RequestBody CohUpdateAnswer newAnswer);

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questionrounds")
    CohQuestionRounds getQuestionRounds(@RequestHeader(AUTHORIZATION) String authorisation,
                                        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                        @PathVariable("onlineHearingId") String onlineHearingId);

    @RequestMapping(method = RequestMethod.POST, value = "/continuous-online-hearings")
    String createOnlineHearing(@RequestHeader(AUTHORIZATION) String authorisation,
                               @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                               CreateOnlineHearingRequest createOnlineHearingRequest);

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings?case_id={caseId}",
            consumes = "application/json")
    CohOnlineHearings getOnlineHearing(@RequestHeader(AUTHORIZATION) String authorisation,
                                       @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                       @PathVariable("caseId") Long caseId);

    @RequestMapping(method = RequestMethod.PUT,
            value = "/continuous-online-hearings/{onlineHearingId}/questions-deadline-extension"
    )
    void extendQuestionRoundDeadline(@RequestHeader(AUTHORIZATION) String oauthToken,
                                     @RequestHeader(SERVICE_AUTHORIZATION) String generate,
                                     @PathVariable("onlineHearingId") String onlineHearingId,
                                     @RequestBody String content
    );
}
