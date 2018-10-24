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
    private final String createdDate;

    public Evidence(String documentLink, String fileName, String createdDate) {
        this.documentLink = documentLink;
        this.fileName = fileName;
        this.createdDate = createdDate;
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

    @ApiModelProperty(example = "2018-10-24'T'12:11:21Z", required = true)
    @JsonProperty(value = "created_date")
    public String getCreatedDate() {
        return createdDate;
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
                Objects.equals(fileName, evidence.fileName) &&
                Objects.equals(createdDate, evidence.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentLink, fileName, createdDate);
    }

    @Override
    public String toString() {
        return "Evidence{" +
                "documentLink='" + documentLink + '\'' +
                ", fileName='" + fileName + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
