package uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

@FeignClient(
        name = "Notiications",
        url = "${notifications.url}"
)

public interface NotificationsClient {
    @PostMapping(value = "/coh-send", produces = "application/json")
    void send(@RequestHeader(AUTHORIZATION) String authorisation,
              @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
              @RequestBody CohEvent event);
}
