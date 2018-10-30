package uk.gov.hmcts.reform.sscscorbackend.service;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_PDF;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscscorbackend.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.domain.onlinehearing.OnlineHearingPdfWraper;
import uk.gov.hmcts.reform.sscscorbackend.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.sscscorbackend.service.onlinehearing.CreateOnlineHearingRequest;

@Service
public class OnlineHearingService {

    private static final Logger LOG = getLogger(RestResponseEntityExceptionHandler.class);
    private static final String HEARING_TYPE_ONLINE_RESOLUTION = "cor";

    private final CohService cohClient;
    private final CcdService ccdService;
    private final IdamService idamService;
    private PDFServiceClient pdfServiceClient;
    private final String appellantTemplatePath;
    private SscsPdfService sscsPdfService;
    private EvidenceUploadService evidenceUploadService;

    public OnlineHearingService(
                                PDFServiceClient pdfServiceClient,
                                @Autowired CohService cohService,
                                @Autowired CcdService ccdService,
                                @Autowired IdamService idamService,
                                @Autowired SscsPdfService sscsPdfService,
                                @Value("${online_hearing_finished.html.template.path}") String appellantTemplatePath
    ) {
        this.cohClient = cohService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.pdfServiceClient = pdfServiceClient;
        this.sscsPdfService = sscsPdfService;
        this.appellantTemplatePath = appellantTemplatePath;
    }

    public String createOnlineHearing(String caseId) {
        CreateOnlineHearingRequest createOnlineHearingRequest =
                new CreateOnlineHearingRequest(caseId);

        //assume need to create it
        return cohClient.createOnlineHearing(createOnlineHearingRequest);
    }

    public Optional<OnlineHearing> getOnlineHearing(String emailAddress) {
        List<SscsCaseDetails> cases = ccdService.findCaseBy(
                ImmutableMap.of("case.subscriptions.appellantSubscription.email", emailAddress),
                idamService.getIdamTokens()
        );

        if (cases == null || cases.isEmpty()) {
            return Optional.empty();
        }
        List<SscsCaseDetails> corCases = filterCorCases(cases);

        if (corCases.isEmpty()) {
            throw new CaseNotCorException();
        } else if (corCases.size() > 1) {
            throw new IllegalStateException("Multiple appeals with online hearings found.");
        }

        return loadOnlineHearingFromCoh(corCases.get(0));
    }

    private List<SscsCaseDetails> filterCorCases(List<SscsCaseDetails> cases) {
        return cases.stream().filter(caseDetails -> caseDetails.getData() != null &&
                caseDetails.getData().getAppeal() != null &&
                HEARING_TYPE_ONLINE_RESOLUTION.equalsIgnoreCase(caseDetails.getData().getAppeal().getHearingType())
        )
                .collect(toList());
    }

    public Optional<Long> getCcdCaseId(String onlineHearingId) {
        CohOnlineHearing onlineHearing = cohClient.getOnlineHearing(onlineHearingId);
        return (onlineHearing != null) ? Optional.of(onlineHearing.getCcdCaseId()) : Optional.empty();
    }

    public void addDecisionReply(String onlineHearingId, TribunalViewResponse tribunalViewResponse) {
        CohDecisionReply cohDecisionReply = new CohDecisionReply(tribunalViewResponse.getReply(), tribunalViewResponse.getReason());
        cohClient.addDecisionReply(onlineHearingId, cohDecisionReply);
    }

    private CohDecisionReply getAppellantDecisionReply(String onlineHearingId) {
        Optional<CohDecisionReplies> decisionRepliesWrapper = cohClient.getDecisionReplies(onlineHearingId);
        if (decisionRepliesWrapper.isPresent()) {
            List<CohDecisionReply> decisionReplies = decisionRepliesWrapper.get().getDecisionReplies()
                    .stream()
                    //.filter(d -> d.getAuthorReference().equals("oauth2Token"))
                    .collect(toList());

            if (decisionReplies.size() > 0) {
                return decisionReplies.get(0);
            }
        }
        return new CohDecisionReply("", "", "", "");
    }

    private Decision getDecision(String onlineHearingId) {
        Optional<CohDecision> decision = cohClient.getDecision(onlineHearingId);
        CohDecisionReply appellantReply = getAppellantDecisionReply(onlineHearingId);
        return decision.map(d -> new Decision(onlineHearingId, d.getDecisionAward(),
                    d.getDecisionHeader(), d.getDecisionReason(),
                    d.getDecisionText(), d.getCurrentDecisionState().getStateName(),
                    d.getCurrentDecisionState().getStateDateTime(),
                    appellantReply.getReply(), appellantReply.getReplyDateTime()))
                .orElse(null);
    }

    private Optional<OnlineHearing> loadOnlineHearingFromCoh(SscsCaseDetails sscsCaseDeails) {
        CohOnlineHearings cohOnlineHearings = cohClient.getOnlineHearing(sscsCaseDeails.getId());

        return cohOnlineHearings.getOnlineHearings().stream()
                .findFirst()
                .map(onlineHearing -> {
                    Name name = sscsCaseDeails.getData().getAppeal().getAppellant().getName();
                    String nameString = name.getFirstName() + " " + name.getLastName();

                    return new OnlineHearing(
                            onlineHearing.getOnlineHearingId(),
                            nameString,
                            sscsCaseDeails.getData().getCaseReference(),
                            getDecision(onlineHearing.getOnlineHearingId())
                    );
                });
    }

    public CohQuestionRounds getQuestionRounds(String onlineHearingId) {
        return cohClient.getQuestionRounds(onlineHearingId);
    }

    public void storeOnlineHearingInCcd(String onlineHearingId, String caseId) {
        LOG.info("Storing online hearing data for case " +caseId + ", hearing " + onlineHearingId);
        //get the questions and answers
        CohQuestionRounds questionRounds = getQuestionRounds(onlineHearingId);

        LOG.info("Got question rounds");

        IdamTokens idamTokens = idamService.getIdamTokens();

        SscsCaseDetails caseDetails = ccdService.getByCaseId(Long.valueOf(caseId), idamTokens);

        LOG.info("Got case details");

        String appellantTitle = caseDetails.getData().getAppeal().getAppellant().getName().getTitle();
        String appellantFirstName = caseDetails.getData().getAppeal().getAppellant().getName().getFirstName();
        String appellantLastName = caseDetails.getData().getAppeal().getAppellant().getName().getLastName();

        String nino = caseDetails.getData().getGeneratedNino();

        String caseReference = caseDetails.getData().getCaseReference();

        OnlineHearingPdfWraper onlineHearingPdfWraper = OnlineHearingPdfWraper.builder()
                .appellantTitle(appellantTitle).appellantFirstName(appellantFirstName)
                .appellantLastName(appellantLastName).cohQuestionRounds(questionRounds)
                .nino(nino).caseReference(caseReference).build();

        Map<String, Object> placeholders = Collections.singletonMap("OnlineHearingPdfWrapper", onlineHearingPdfWraper);

        byte[] pdfBytes = createPdf(placeholders);

        String fileName = "COR Transcript - " + caseReference + ".pdf";
        ByteArrayMultipartFile file = ByteArrayMultipartFile.builder().content(pdfBytes).name(fileName).contentType(APPLICATION_PDF).build();

        getEvidenceUploadService().uploadEvidence(caseId, file);

        sscsPdfService.mergeDocIntoCcd(fileName, pdfBytes,
                Long.valueOf(caseId), caseDetails.getData(), idamTokens);
    }

    public byte[] createPdf(Map<String,Object> placeholders) {
        byte[] template;
        try {
            template = getTemplate();
        } catch (IOException e) {
            throw new PdfGenerationException("Error getting template " + appellantTemplatePath, e);
        }

        return pdfServiceClient.generateFromHtml(template, placeholders);

    }

    @Autowired
    public void setEvidenceUploadService(EvidenceUploadService evidenceUploadService) {
        this.evidenceUploadService = evidenceUploadService;
    }

    public EvidenceUploadService getEvidenceUploadService() {
        return evidenceUploadService;
    }

    private byte[] getTemplate() throws IOException {
        InputStream in = getClass().getResourceAsStream(appellantTemplatePath);
        return IOUtils.toByteArray(in);
    }


}
