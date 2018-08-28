package uk.gov.hmcts.reform.sscscorbackend.service;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.sscscorbackend.config.CohRequestConfiguration;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;


@FeignClient(
        name = "Coh",
        url = "${coh.url}",
        decode404 = true,
        configuration = CohRequestConfiguration.class
)
public interface CohClient {

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}")
    CohQuestion getQuestion(@PathVariable("onlineHearingId") String onlineHearingId, @PathVariable("questionId") String questionId);

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers")
    List<CohAnswer> getAnswers(@PathVariable("onlineHearingId") String onlineHearingId, @PathVariable("questionId") String questionId);

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers"
    )
    void createAnswer(@PathVariable("onlineHearingId") String onlineHearingId,
                      @PathVariable("questionId") String questionId,
                      @RequestBody CohUpdateAnswer newAnswer);

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}/answers/{answerId}"
    )
    void updateAnswer(@PathVariable("onlineHearingId") String onlineHearingId,
                      @PathVariable("questionId") String questionId,
                      @PathVariable("answerId") String answerId,
                      @RequestBody CohUpdateAnswer newAnswer);

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questionrounds")
    CohQuestionRounds getQuestionRounds(@PathVariable("onlineHearingId") String onlineHearingId);

    @RequestMapping(method = RequestMethod.POST, value = "/continuous-online-hearings")
    String createOnlineHearing(CreateOnlineHearingRequest createOnlineHearingRequest);

    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings?case_id={caseId}",
            consumes = "application/json")
    CohOnlineHearings getOnlineHearing(@PathVariable("caseId") Long caseId);
}
