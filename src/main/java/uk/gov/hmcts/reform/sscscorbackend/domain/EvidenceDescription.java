package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvidenceDescription {
    @ApiModelProperty(example = "this is a description of the evidence", required = false)
    @JsonProperty(value = "body")
    private String body;

    // needed for Jackson
    private EvidenceDescription() {
    }

    public EvidenceDescription(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
