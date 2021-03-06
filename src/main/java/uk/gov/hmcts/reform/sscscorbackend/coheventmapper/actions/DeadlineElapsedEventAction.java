package uk.gov.hmcts.reform.sscscorbackend.coheventmapper.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionRound;
import uk.gov.hmcts.reform.sscscorbackend.service.QuestionService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.CorEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.email.EmailMessageBuilder;
import uk.gov.hmcts.reform.sscscorbackend.service.evidence.EvidenceUploadEmailService;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.StoreAnswersDeadlineElapsedPdfService;

@Service
public class DeadlineElapsedEventAction extends QuestionRoundEndedAction {

    @Autowired
    public DeadlineElapsedEventAction(
            CorEmailService corEmailService,
            StoreAnswersDeadlineElapsedPdfService storeQuestionsPdfService,
            EmailMessageBuilder emailMessageBuilder,
            EvidenceUploadEmailService evidenceUploadEmailService,
            QuestionService questionService) {
        super(storeQuestionsPdfService, corEmailService, emailMessageBuilder, evidenceUploadEmailService, questionService);
    }

    @Override
    public String cohEvent() {
        return "question_deadline_elapsed";
    }

    @Override
    public EventType getCcdEventType() {
        return EventType.COH_QUESTION_DEADLINE_ELAPSED;
    }

    @Override
    boolean shouldHandleQuestionRound(QuestionRound questions) {
        return true;
    }
}
