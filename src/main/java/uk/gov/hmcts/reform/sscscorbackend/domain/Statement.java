package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Statement {
    @ApiModelProperty(example = "this is the text of a personal statement", required = true)
    @JsonProperty(value = "body")
    private String body;

    // needed for Jackson
    private Statement() {
    }

    public Statement(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
