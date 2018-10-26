package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;

@Service
public class EvidenceUploadService {
    private final EvidenceManagementService evidenceManagementService;
    private final CcdService ccdService;
    private final IdamService idamService;
    private final OnlineHearingService onlineHearingService;

    public EvidenceUploadService(EvidenceManagementService evidenceManagementService, CcdService ccdService, IdamService idamService, OnlineHearingService onlineHearingService) {
        this.evidenceManagementService = evidenceManagementService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.onlineHearingService = onlineHearingService;
    }

    public Optional<Evidence> uploadEvidence(String onlineHearingId, String questionId, MultipartFile file) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .map(ccdCaseId -> {
                    Document document = uploadDocument(file);
                    addDocumentToCcd(questionId, ccdCaseId, document);

                    return new Evidence(document.links.self.href, document.originalDocumentName, getCreatedDate(document));
                });
    }

    public List<Evidence> listEvidence(String onlineHearingId, String questionId) {
        return listEvidence(onlineHearingId).getOrDefault(questionId, emptyList());
    }

    public Map<String, List<Evidence>> listEvidence(String onlineHearingId) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .<Map<String, List<Evidence>>>map(ccdCaseId -> {
                    IdamTokens idamTokens = idamService.getIdamTokens();
                    SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);

                    List<CorDocument> corDocuments = caseDetails.getData().getCorDocument();
                    if (corDocuments == null) {
                        return emptyMap();
                    }

                    return corDocuments.stream().collect(groupingBy(
                        corDocumentDetails -> corDocumentDetails.getValue().getQuestionId(),
                        mapping(corDocumentToEvidence(), toList()))
                    );
                }).orElse(emptyMap());
    }

    private Function<CorDocument, Evidence> corDocumentToEvidence() {
        return corDocument -> {
            SscsDocumentDetails sscsDocumentDetails = corDocument.getValue().getDocument();
            DocumentLink documentLink = sscsDocumentDetails.getDocumentLink();
            return new Evidence(
                    documentLink != null ? documentLink.getDocumentUrl() : null,
                    sscsDocumentDetails.getDocumentFileName(),
                    sscsDocumentDetails.getDocumentDateAdded());
        };
    }

    private SscsCaseDetails getSscsCaseDetails(Long optionalCcdCaseId, IdamTokens idamTokens) {
        SscsCaseDetails caseDetails = ccdService.getByCaseId(optionalCcdCaseId, idamTokens);

        if (caseDetails == null) {
            throw new IllegalStateException("Online hearing for ccdCaseId [" + optionalCcdCaseId + "] found but cannot find the case in CCD");
        }
        return caseDetails;
    }

    private Document uploadDocument(MultipartFile file) {
        return evidenceManagementService.upload(singletonList(file))
                .getEmbedded()
                .getDocuments()
                .get(0);
    }

    private void addDocumentToCcd(String questionId, Long ccdCaseId, Document document) {
        IdamTokens idamTokens = idamService.getIdamTokens();
        SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);

        addNewCorDocumentToCaseDetails(questionId, document, caseDetails);

        ccdService.updateCase(caseDetails.getData(), ccdCaseId, "uploadCorDocument", "SSCS - cor evidence uploaded", "Updated SSCS", idamTokens);
    }

    private void addNewCorDocumentToCaseDetails(String questionId, Document document, SscsCaseDetails caseDetails) {
        List<CorDocument> currentCorDocuments = caseDetails.getData().getCorDocument();
        ArrayList<CorDocument> newCorDocuments =
                (currentCorDocuments == null) ? new ArrayList<>() : new ArrayList<>(currentCorDocuments);
        newCorDocuments.add(createNewCorDocument(questionId, document));

        caseDetails.getData().setCorDocument(newCorDocuments);
    }

    private CorDocument createNewCorDocument(String questionId, Document document) {
        String createdOn = getCreatedDate(document);
        DocumentLink documentLink = DocumentLink.builder()
                .documentUrl(document.links.self.href)
                .build();

        SscsDocumentDetails sscsDocument = new SscsDocumentDetails(
                "Other evidence", document.originalDocumentName, null, createdOn, documentLink, null
        );

        return new CorDocument(new CorDocumentDetails(sscsDocument, questionId));
    }

    private String getCreatedDate(Document document) {
        return document.createdOn.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_DATE);
    }

    public boolean deleteEvidence(String onlineHearingId, String evidenceId) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .map(ccdCaseId -> {
                    IdamTokens idamTokens = idamService.getIdamTokens();
                    SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);
                    List<CorDocument> corDocuments = caseDetails.getData().getCorDocument();

                    if (corDocuments != null) {
                        List<CorDocument> newCorDocuments = corDocuments.stream()
                                .filter(corDocument -> !corDocument.getValue().getDocument().getDocumentLink().getDocumentUrl().endsWith(evidenceId))
                                .collect(toList());
                        caseDetails.getData().setCorDocument(newCorDocuments);

                        ccdService.updateCase(caseDetails.getData(), ccdCaseId, "uploadCorDocument", "SSCS - cor evidence deleted", "Updated SSCS", idamTokens);
                    }
                    return true;
                }).orElse(false);
    }
}
