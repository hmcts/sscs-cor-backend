package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.ListUtils.union;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.documentmanagement.DocumentManagementService;

@Slf4j
@Service
public class EvidenceUploadService {
    private final DocumentManagementService documentManagementService;
    private final CorCcdService ccdService;
    private final IdamService idamService;
    private OnlineHearingService onlineHearingService;

    private static final String UPDATED_SSCS = "Updated SSCS";
    private static final String UPLOAD_COR_DOCUMENT = "uploadCorDocument";

    private static final DraftHearingDocumentExtractor draftHearingDocumentExtractor = new DraftHearingDocumentExtractor();
    private static final QuestionDocumentExtractor questionDocumentExtractor = new QuestionDocumentExtractor();

    @Autowired
    public EvidenceUploadService(DocumentManagementService documentManagementService, CorCcdService ccdService, IdamService idamService, OnlineHearingService onlineHearingService) {
        this.documentManagementService = documentManagementService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.onlineHearingService = onlineHearingService;
    }

    public Optional<Evidence> uploadDraftHearingEvidence(String onlineHearingId, MultipartFile file) {
        return uploadEvidence(onlineHearingId, file, draftHearingDocumentExtractor, document -> new SscsDocument(createNewDocumentDetails(document)));
    }

    public Optional<Evidence> uploadQuestionEvidence(String onlineHearingId, String questionId, MultipartFile file) {
        return uploadEvidence(onlineHearingId, file, questionDocumentExtractor, document -> new CorDocument(new CorDocumentDetails(createNewDocumentDetails(document), questionId)));
    }

    private SscsDocumentDetails createNewDocumentDetails(Document document) {
        String createdOn = getCreatedDate(document);
        DocumentLink documentLink = DocumentLink.builder()
                .documentUrl(document.links.self.href)
                .build();

        return new SscsDocumentDetails(
                "Other evidence", document.originalDocumentName, null, createdOn, documentLink, null
        );
    }

    private String getCreatedDate(Document document) {
        return document.createdOn.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_DATE);
    }

    private <E> Optional<Evidence> uploadEvidence(String onlineHearingId, MultipartFile file, DocumentExtract<E> documentExtract, Function<Document, E> createNewDocument) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .map(ccdCaseId -> {
                    Document document = uploadDocument(file);
                    log.info("Upload document for case {} ...", ccdCaseId);
                    IdamTokens idamTokens = idamService.getIdamTokens();
                    log.info("Adding document to case {} ...", ccdCaseId);

                    SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);

                    List<E> currentDocuments = documentExtract.getDocuments().apply(caseDetails.getData());
                    ArrayList<E> newDocuments = (currentDocuments == null) ? new ArrayList<>() : new ArrayList<>(currentDocuments);
                    newDocuments.add(createNewDocument.apply(document));

                    documentExtract.setDocuments().accept(caseDetails.getData(), newDocuments);


                    ccdService.updateCase(caseDetails.getData(), ccdCaseId, UPLOAD_COR_DOCUMENT, "SSCS - cor evidence uploaded", UPDATED_SSCS, idamTokens);

                    return new Evidence(document.links.self.href, document.originalDocumentName, getCreatedDate(document));
                });
    }

    private Document uploadDocument(MultipartFile file) {
        return documentManagementService.upload(singletonList(file))
                .getEmbedded()
                .getDocuments()
                .get(0);
    }

    public boolean submitHearingEvidence(String onlineHearingId) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .map(ccdCaseId -> {
                    log.info("Submitting draft document for case [" + ccdCaseId + "]");

                    IdamTokens idamTokens = idamService.getIdamTokens();
                    SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);

                    SscsCaseData sscsCaseData = caseDetails.getData();
                    List<SscsDocument> draftSscsDocument = sscsCaseData.getDraftSscsDocument();

                    List<SscsDocument> newSscsDocumentsList = union(
                            emptyIfNull(sscsCaseData.getSscsDocument()),
                            emptyIfNull(draftSscsDocument)
                    );
                    sscsCaseData.setSscsDocument(newSscsDocumentsList);
                    sscsCaseData.setDraftSscsDocument(Collections.emptyList());

                    ccdService.updateCase(sscsCaseData, ccdCaseId, UPLOAD_COR_DOCUMENT, "SSCS - cor evidence uploaded", UPDATED_SSCS, idamTokens);

                    return true;
                })
                .orElse(false);
    }

    public List<Evidence> listDraftHearingEvidence(String onlineHearingId) {
        return loadEvidence(onlineHearingId)
                .map(LoadedEvidence::getDraftHearingEvidence)
                .orElse(emptyList());
    }

    public List<Evidence> listQuestionEvidence(String onlineHearingId, String questionId) {
        return listQuestionEvidence(onlineHearingId).getOrDefault(questionId, emptyList());
    }

    public Map<String, List<Evidence>> listQuestionEvidence(String onlineHearingId) {
        return loadEvidence(onlineHearingId)
                .map(LoadedEvidence::getQuestionEvidence)
                .orElse(emptyMap());
    }

    private Optional<LoadedEvidence> loadEvidence(String onlineHearingId) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .map(ccdCaseId -> {
                    IdamTokens idamTokens = idamService.getIdamTokens();
                    SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);

                    return new LoadedEvidence(caseDetails);
                });
    }

    public boolean deleteQuestionEvidence(String onlineHearingId, String evidenceId) {
        return deleteEvidence(onlineHearingId, evidenceId, questionDocumentExtractor);
    }

    public boolean deleteDraftHearingEvidence(String onlineHearingId, String evidenceId) {
        return deleteEvidence(onlineHearingId, evidenceId, draftHearingDocumentExtractor);
    }

    private <E> boolean deleteEvidence(String onlineHearingId, String evidenceId, DocumentExtract<E> documentExtract) {
        return onlineHearingService.getCcdCaseId(onlineHearingId)
                .map(ccdCaseId -> {
                    IdamTokens idamTokens = idamService.getIdamTokens();
                    SscsCaseDetails caseDetails = getSscsCaseDetails(ccdCaseId, idamTokens);
                    List<E> documents = documentExtract.getDocuments().apply(caseDetails.getData());

                    if (documents != null) {
                        List<E> newDocuments = documents.stream()
                                .filter(corDocument -> !documentExtract.findDocument().apply(corDocument).getDocumentLink().getDocumentUrl().endsWith(evidenceId))
                                .collect(toList());
                        documentExtract.setDocuments().accept(caseDetails.getData(), newDocuments);

                        ccdService.updateCase(caseDetails.getData(), ccdCaseId, UPLOAD_COR_DOCUMENT, "SSCS - cor evidence deleted", UPDATED_SSCS, idamTokens);

                        documentManagementService.delete(evidenceId);
                    }
                    return true;
                }).orElse(false);
    }

    private interface DocumentExtract<E> {
        Function<SscsCaseData, List<E>> getDocuments();

        BiConsumer<SscsCaseData, List<E>> setDocuments();

        Function<E, SscsDocumentDetails> findDocument();
    }

    private static class QuestionDocumentExtractor implements DocumentExtract<CorDocument> {
        @Override
        public Function<SscsCaseData, List<CorDocument>> getDocuments() {
            return SscsCaseData::getCorDocument;
        }

        @Override
        public BiConsumer<SscsCaseData, List<CorDocument>> setDocuments() {
            return SscsCaseData::setCorDocument;
        }

        @Override
        public Function<CorDocument, SscsDocumentDetails> findDocument() {
            return document -> document.getValue().getDocument();
        }
    }

    private static class DraftHearingDocumentExtractor implements DocumentExtract<SscsDocument> {
        @Override
        public Function<SscsCaseData, List<SscsDocument>> getDocuments() {
            return SscsCaseData::getDraftSscsDocument;
        }

        @Override
        public BiConsumer<SscsCaseData, List<SscsDocument>> setDocuments() {
            return SscsCaseData::setDraftSscsDocument;
        }

        @Override
        public Function<SscsDocument, SscsDocumentDetails> findDocument() {
            return SscsDocument::getValue;
        }
    }

    private SscsCaseDetails getSscsCaseDetails(Long optionalCcdCaseId, IdamTokens idamTokens) {
        SscsCaseDetails caseDetails = ccdService.getByCaseId(optionalCcdCaseId, idamTokens);

        if (caseDetails == null) {
            throw new IllegalStateException("Online hearing for ccdCaseId [" + optionalCcdCaseId + "] found but cannot find the case in CCD");
        }
        return caseDetails;
    }

    private static class LoadedEvidence {
        private final SscsCaseDetails caseDetails;
        private final List<Evidence> draftHearingEvidence;
        private final List<Evidence> hearingEvidence;
        private final Map<String, List<Evidence>> draftQuestionEvidence;
        private final Map<String, List<Evidence>> questionEvidence;

        LoadedEvidence(SscsCaseDetails caseDetails) {
            this.caseDetails = caseDetails;
            draftHearingEvidence = extractHearingEvidences(caseDetails.getData().getDraftSscsDocument());
            hearingEvidence = extractHearingEvidences(caseDetails.getData().getSscsDocument());
            draftQuestionEvidence = extractQuestionEvidence(caseDetails.getData().getDraftCorDocument());
            questionEvidence = extractQuestionEvidence(caseDetails.getData().getCorDocument());
        }

        public SscsCaseDetails getCaseDetails() {
            return caseDetails;
        }

        public List<Evidence> getDraftHearingEvidence() {
            return draftHearingEvidence;
        }

        public List<Evidence> getHearingEvidence() {
            return hearingEvidence;
        }

        public Map<String, List<Evidence>> getDraftQuestionEvidence() {
            return draftQuestionEvidence;
        }

        public Map<String, List<Evidence>> getQuestionEvidence() {
            return questionEvidence;
        }

        private List<Evidence> extractHearingEvidences(List<SscsDocument> sscsDocuments) {
            List<SscsDocument> hearingDocuments = emptyIfNull(sscsDocuments);
            return hearingDocuments.stream().map(sscsDocumentToEvidence()).collect(toList());
        }

        private Map<String, List<Evidence>> extractQuestionEvidence(List<CorDocument> corDocument) {
            List<CorDocument> questionDocuments = emptyIfNull(corDocument);
            return questionDocuments.stream()
                    .collect(groupingBy(corDocumentDetails -> corDocumentDetails.getValue().getQuestionId(), mapping(corDocumentToEvidence(), toList())));
        }

        private Function<CorDocument, Evidence> corDocumentToEvidence() {
            return corDocument -> {
                SscsDocumentDetails sscsDocumentDetails = corDocument.getValue().getDocument();
                return extractEvidence(sscsDocumentDetails);
            };
        }

        private Function<SscsDocument, Evidence> sscsDocumentToEvidence() {
            return sscsDocument -> {
                SscsDocumentDetails sscsDocumentDetails = sscsDocument.getValue();
                return extractEvidence(sscsDocumentDetails);
            };
        }

        private Evidence extractEvidence(SscsDocumentDetails sscsDocumentDetails) {
            DocumentLink documentLink = sscsDocumentDetails.getDocumentLink();
            return new Evidence(
                    documentLink != null ? documentLink.getDocumentUrl() : null,
                    sscsDocumentDetails.getDocumentFileName(),
                    sscsDocumentDetails.getDocumentDateAdded());
        }
    }
}
