package uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PanelRequest {

    public PanelRequest(@JsonProperty(value = "identity_token") String identityToken,
                        @JsonProperty(value = "name") String name,
                        @JsonProperty(value = "role") String role) {
        this.identityToken = identityToken;
        this.name = name;
        this.role = role;
    }

    @JsonProperty(value = "identity_token")
    String identityToken;

    @JsonProperty(value = "name")
    String name;

    @JsonProperty(value = "role")
    String role;

    public String getIdentityToken() {
        return identityToken;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
