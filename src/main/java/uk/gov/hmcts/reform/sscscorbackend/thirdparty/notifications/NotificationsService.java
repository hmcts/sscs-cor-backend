package uk.gov.hmcts.reform.sscscorbackend.thirdparty.notifications;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.coh.apinotifications.CohEvent;

@Service
public class NotificationsService {
    private final IdamService idamService;
    private final NotificationsClient notificationsClient;

    public NotificationsService(IdamService idamService, NotificationsClient notificationsClient) {
        this.idamService = idamService;
        this.notificationsClient = notificationsClient;
    }

    public void send(CohEvent cohEvent) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        notificationsClient.send(
                idamTokens.getIdamOauth2Token(),
                idamTokens.getServiceAuthorization(),
                cohEvent
        );
    }
}
