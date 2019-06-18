package uk.gov.hmcts.reform.sscscorbackend.service.evidence;

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
import uk.gov.hmcts.reform.sscscorbackend.domain.Evidence;
import uk.gov.hmcts.reform.sscscorbackend.domain.EvidenceDescription;
import uk.gov.hmcts.reform.sscscorbackend.domain.Question;
import uk.gov.hmcts.reform.sscscorbackend.service.OnlineHearingService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.CohEventActionContext;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreEvidenceDescriptionService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.data.EvidenceDescriptionPdfData;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.ccd.CorCcdService;
import uk.gov.hmcts.reform.sscscorbackend.thirdparty.documentmanagement.DocumentManagementService;

@Slf4j
@Service
public class EvidenceUploadService {
    private final DocumentManagementService documentManagementService;
    private final CorCcdService ccdService;
    private final IdamService idamService;
    private final OnlineHearingService onlineHearingService;
    private final StoreEvidenceDescriptionService storeEvidenceDescriptionService;
    private final EvidenceUploadEmailService evidenceUploadEmailService;

    private static final String UPDATED_SSCS = "Updated SSCS";
    private static final String UPLOAD_COR_DOCUMENT = "uploadCorDocument";

    private static final DraftHearingDocumentExtractor draftHearingDocumentExtractor = new DraftHearingDocumentExtractor();
    private static final QuestionDocumentExtractor questionDocumentExtractor = new QuestionDocumentExtractor();

    @Autowired
    public EvidenceUploadService(DocumentManagementService documentManagementService, CorCcdService ccdService, IdamService idamService, OnlineHearingService onlineHearingService, StoreEvidenceDescriptionService storeEvidenceDescriptionService, EvidenceUploadEmailService evidenceUploadEmailService) {
        this.documentManagementService = documentManagementService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.onlineHearingService = onlineHearingService;
        this.storeEvidenceDescriptionService = storeEvidenceDescriptionService;
        this.evidenceUploadEmailService = evidenceUploadEmailService;
    }

    public Optional<Evidence> uploadDraftHearingEvidence(String onlineHearingId, MultipartFile file) {
        return uploadEvidence(onlineHearingId, file, draftHearingDocumentExtractor, document -> new SscsDocument(createNewDocumentDetails(document)));
    }

    public Optional<Evidence> uploadDraftQuestionEvidence(String onlineHearingId, String questionId, MultipartFile file) {
        return uploadEvidence(onlineHearingId, file, questionDocumentExtractor, document -> new CorDocument(new CorDocumentDetails(createNewDocumentDetails(document), questionId)));
    }

    private SscsDocumentDetails createNewDocumentDetails(Document document) {
        String createdOn = getCreatedDate(document);
        DocumentLink documentLink = DocumentLink.builder()
                .documentUrl(document.links.self.href)
                .build();

        return SscsDocumentDetails.builder()
                .documentType("Other evidence")
                .documentFileName(document.originalDocumentName)
                .documentDateAdded(createdOn)
                .documentLink(documentLink)
                .build();
    }

    private String getCreatedDate(Document document) {
        return document.createdOn.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_DATE);
    }

    private <E> Optional<Evidence> uploadEvidence(String onlineHearingId, MultipartFile file, DocumentExtract<E> documentExtract, Function<Document, E> createNewDocument) {
        return onlineHearingService.getCcdCase(onlineHearingId)
                .map(caseDetails -> {
                    Document document = uploadDocument(file);

                    List<E> currentDocuments = documentExtract.getDocuments().apply(caseDetails.getData());
                    ArrayList<E> newDocuments = (currentDocuments == null) ? new ArrayList<>() : new ArrayList<>(currentDocuments);
                    newDocuments.add(createNewDocument.apply(document));

                    documentExtract.setDocuments().accept(caseDetails.getData(), newDocuments);


                    ccdService.updateCase(caseDetails.getData(), caseDetails.getId(), UPLOAD_COR_DOCUMENT, "SSCS - cor evidence uploaded", UPDATED_SSCS, idamService.getIdamTokens());

                    return new Evidence(document.links.self.href, document.originalDocumentName, getCreatedDate(document));
                });
    }

    private Document uploadDocument(MultipartFile file) {
        return documentManagementService.upload(singletonList(file))
                .getEmbedded()
                .getDocuments()
                .get(0);
    }

    public boolean submitHearingEvidence(String onlineHearingId, EvidenceDescription description) {
        return onlineHearingService.getCcdCase(onlineHearingId)
                .map(caseDetails -> {
                    Long ccdCaseId = caseDetails.getId();
                    log.info("Submitting draft document for case [" + ccdCaseId + "]");

                    SscsCaseData sscsCaseData = caseDetails.getData();

                    EvidenceDescriptionPdfData data = new EvidenceDescriptionPdfData(caseDetails, description, getFileNames(sscsCaseData));
                    CohEventActionContext storePdfContext = storeEvidenceDescriptionService.storePdf(ccdCaseId, onlineHearingId, data);

                    List<SscsDocument> draftSscsDocument = storePdfContext.getDocument().getData().getDraftSscsDocument();
                    List<SscsDocument> newSscsDocumentsList = union(
                            emptyIfNull(storePdfContext.getDocument().getData().getSscsDocument()),
                            emptyIfNull(draftSscsDocument)
                    );

                    sscsCaseData.setSscsDocument(newSscsDocumentsList);
                    sscsCaseData.setDraftSscsDocument(Collections.emptyList());

                    ccdService.updateCase(sscsCaseData, ccdCaseId, UPLOAD_COR_DOCUMENT, "SSCS - cor evidence uploaded", UPDATED_SSCS, idamService.getIdamTokens());

                    evidenceUploadEmailService.sendToDwp(storePdfContext.getPdf(), draftSscsDocument, caseDetails);

                    return true;
                })
                .orElse(false);
    }

    public boolean submitQuestionEvidence(String onlineHearingId, Question question) {
        return onlineHearingService.getCcdCase(onlineHearingId)
                .map(caseDetails -> {
                    Long ccdCaseId = caseDetails.getId();
                    log.info("Submitting draft document for case [" + ccdCaseId + "] and question [" + question.getQuestionId() + "]");

                    SscsCaseData sscsCaseData = caseDetails.getData();


                    List<CorDocument> draftCorDocument = sscsCaseData.getDraftCorDocument();

                    if (draftCorDocument != null && !draftCorDocument.isEmpty()) {
                        Map<Boolean, List<CorDocument>> draftCorDocumentsForQuestionId = draftCorDocument.stream()
                                .collect(partitioningBy(corDocument -> corDocument.getValue().getQuestionId().equals(question.getQuestionId())));

                        List<CorDocument> newCorDocumentList = union(
                                emptyIfNull(sscsCaseData.getCorDocument()),
                                emptyIfNull(draftCorDocumentsForQuestionId.get(true))
                        );
                        sscsCaseData.setCorDocument(newCorDocumentList);
                        sscsCaseData.setDraftCorDocument(draftCorDocumentsForQuestionId.get(false));
                        ccdService.updateCase(sscsCaseData, ccdCaseId, UPLOAD_COR_DOCUMENT, "SSCS - cor evidence uploaded", UPDATED_SSCS, idamService.getIdamTokens());
                    }
                    return true;
                })
                .orElse(false);
    }

    private List<String> getFileNames(SscsCaseData sscsCaseData) {
        return sscsCaseData.getDraftSscsDocument().stream()
                .map(document -> document.getValue().getDocumentFileName())
                .collect(toList());
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
        Optional<LoadedEvidence> loadedEvidence = loadEvidence(onlineHearingId);

        Map<String, List<Evidence>> draftEvidence = loadedEvidence
                .map(LoadedEvidence::getDraftQuestionEvidence)
                .orElse(emptyMap());
        Map<String, List<Evidence>> evidence = loadedEvidence
                .map(LoadedEvidence::getQuestionEvidence)
                .orElse(emptyMap());
        HashMap<String, List<Evidence>> combinedEvidence = new HashMap<>();
        combinedEvidence.putAll(draftEvidence);
        combinedEvidence.putAll(evidence);

        return combinedEvidence;
    }

    private Optional<LoadedEvidence> loadEvidence(String onlineHearingId) {
        return onlineHearingService.getCcdCase(onlineHearingId)
                .map(LoadedEvidence::new);
    }

    public boolean deleteQuestionEvidence(String onlineHearingId, String evidenceId) {
        return deleteEvidence(onlineHearingId, evidenceId, questionDocumentExtractor);
    }

    public boolean deleteDraftHearingEvidence(String onlineHearingId, String evidenceId) {
        return deleteEvidence(onlineHearingId, evidenceId, draftHearingDocumentExtractor);
    }

    private <E> boolean deleteEvidence(String onlineHearingId, String evidenceId, DocumentExtract<E> documentExtract) {
        return onlineHearingService.getCcdCase(onlineHearingId)
                .map(caseDetails -> {
                    List<E> documents = documentExtract.getDocuments().apply(caseDetails.getData());

                    if (documents != null) {
                        List<E> newDocuments = documents.stream()
                                .filter(corDocument -> !documentExtract.findDocument().apply(corDocument).getDocumentLink().getDocumentUrl().endsWith(evidenceId))
                                .collect(toList());
                        documentExtract.setDocuments().accept(caseDetails.getData(), newDocuments);

                        ccdService.updateCase(caseDetails.getData(), caseDetails.getId(), UPLOAD_COR_DOCUMENT, "SSCS - cor evidence deleted", UPDATED_SSCS, idamService.getIdamTokens());

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
            return SscsCaseData::getDraftCorDocument;
        }

        @Override
        public BiConsumer<SscsCaseData, List<CorDocument>> setDocuments() {
            return SscsCaseData::setDraftCorDocument;
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
