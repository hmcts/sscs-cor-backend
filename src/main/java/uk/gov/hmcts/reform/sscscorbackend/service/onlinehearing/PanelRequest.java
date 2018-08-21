package uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PanelRequest {

    @JsonCreator
    public PanelRequest(@JsonProperty(value = "identity_token") String identityToken,
                        @JsonProperty(value = "name") String name,
                        @JsonProperty(value = "role") String role) {
        this.identityToken = identityToken;
        this.name = name;
        this.role = role;
    }

    private String identityToken;

    private String name;

    private String role;

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
