package uk.gov.hmcts.reform.sscscorbackend.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;

@FeignClient(name = "Coh", url = "${coh.url}")
public interface CohClient {
    @RequestMapping(method = RequestMethod.GET, value = "/continuous-online-hearings/{onlineHearingId}/questions/{questionId}")
    Question getQuestion(@PathVariable("onlineHearingId") String onlineHearingId, @PathVariable("questionId") String questionId);

}
