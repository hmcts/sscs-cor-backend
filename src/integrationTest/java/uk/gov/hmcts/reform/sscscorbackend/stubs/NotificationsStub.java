package uk.gov.hmcts.reform.sscscorbackend.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class NotificationsStub extends BaseStub {
    public NotificationsStub(String url) {
        super(url);
    }

    public void stubSendNotification(String cohEvent) {
        wireMock.stubFor(post("/coh-send")
                .withRequestBody(equalToJson(cohEvent))
                .willReturn(ok()));
    }

    public void verifySendNotification(String cohEvent) {
        verifyAsync(postRequestedFor(urlEqualTo("/coh-send"))
                .withRequestBody(equalToJson(cohEvent))
        );
    }
}
