package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociateCaseDetails {
    @ApiModelProperty(example = "subscription email address", required = true)
    @JsonProperty(value = "email")
    private String email;
    @ApiModelProperty(example = "appellant postcode", required = true)
    @JsonProperty(value = "postcode")
    private String postcode;

    //Needed for Jackson
    private AssociateCaseDetails() {

    }

    public AssociateCaseDetails(String email, String postcode) {
        this.email = email;
        this.postcode = postcode;
    }

    public String getEmail() {
        return email;
    }

    public String getPostcode() {
        return postcode;
    }
}
