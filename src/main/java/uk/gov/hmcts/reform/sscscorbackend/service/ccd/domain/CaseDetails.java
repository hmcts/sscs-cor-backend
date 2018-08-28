package uk.gov.hmcts.reform.sscscorbackend.service.ccd.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {
    private Long id;
    private String jurisdiction;
    private String caseTypeId;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String state;
    private Integer lockedBy;
    private Integer securityLevel;
    private SscsCaseData data;
    private Classification securityClassification;
    private String callbackResponseStatus;
}