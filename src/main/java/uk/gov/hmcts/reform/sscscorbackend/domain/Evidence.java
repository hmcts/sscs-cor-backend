package uk.gov.hmcts.reform.sscscorbackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Evidence {
    private final String documentLink;
    private final String fileName;

    public Evidence(String documentLink, String fileName) {
        this.documentLink = documentLink;
        this.fileName = fileName;
    }

    @ApiModelProperty(example = "http://dm-store-aat.service.core-compute-aat.internal/documents/8f79deb3-5d7a-4e6f-846a-a8131ac6a3bb", required = true)
    @JsonProperty(value = "document_link")
    public String getDocumentLink() {
        return documentLink;
    }

    @ApiModelProperty(example = "some_file_name.txt", required = true)
    @JsonProperty(value = "file_name")
    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Evidence evidence = (Evidence) o;
        return Objects.equals(documentLink, evidence.documentLink) &&
                Objects.equals(fileName, evidence.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentLink, fileName);
    }

    @Override
    public String toString() {
        return "Evidence{" +
                "documentLink='" + documentLink + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
