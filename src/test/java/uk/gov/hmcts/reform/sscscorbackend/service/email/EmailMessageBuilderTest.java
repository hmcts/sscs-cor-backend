package uk.gov.hmcts.reform.sscscorbackend.service.email;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscscorbackend.DataFixtures;
import uk.gov.hmcts.reform.sscscorbackend.domain.QuestionSummary;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.util.PdfDateUtil;

public class EmailMessageBuilderTest {

    private EmailMessageBuilder emailMessageBuilder;
    private SscsCaseDetails caseDetails;

    @Before
    public void setUp() {
        emailMessageBuilder = new EmailMessageBuilder();
        caseDetails = SscsCaseDetails.builder()
                .id(12345678L)
                .data(SscsCaseData.builder()
                        .appeal(Appeal.builder()
                                .appellant(Appellant.builder()
                                        .name(Name.builder()
                                                .firstName("Jean")
                                                .lastName("Valjean")
                                                .build())
                                        .identity(Identity.builder().nino("JV123456").build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @Test
    public void buildAnswersContent() {
        String message = emailMessageBuilder.getAnswerMessage(caseDetails);

        assertThat(message, is("Appeal reference number: 12345678\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "Dear DWP\n" +
                "\n" +
                "The appellant has submitted additional information in relation to the above appeal. " +
                "Please see attached.\n" +
                "\n" +
                "Please respond to this email if you have any comment.\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }

    @Test
    public void buildQuestionContent() {
        String message = new EmailMessageBuilder().getQuestionMessage(caseDetails);

        assertThat(message, is("Appeal reference number: 12345678\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "Dear DWP\n" +
                "\n" +
                "The tribunal have sent some questions to the appellant in the above appeal.\n" +
                "Please see the questions attached.\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }

    @Test
    public void buildRelistingContent() {
        String message = new EmailMessageBuilder().getRelistedMessage(caseDetails);

        assertThat(message, is("Hearing required\n" +
                "\n" +
                "Appeal reference number: 12345678\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "Dear DWP\n" +
                "\n" +
                "The tribunal panel have decided that a hearing is required for the above appeal. " +
                "A hearing will be booked and details will be sent.\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }

    @Test
    public void buildDecisionIssued() {
        String message = new EmailMessageBuilder().getDecisionIssuedMessage(caseDetails);

        assertThat(message, is("Preliminary view offered\n" +
                "\n" +
                "Appeal reference number: 12345678\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "Dear DWP\n" +
                "\n" +
                "The tribunal panel have reached a view on the above appeal.\n" +
                "\n" +
                "The view is attached to this email. Please read it and reply, stating whether you agree or " +
                "disagree with it. Please provide reasons if you disagree.\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }

    @Test
    public void buildDecisionAccepted() {
        String message = new EmailMessageBuilder().getDecisionAcceptedMessage(caseDetails, "someUrl");

        assertThat(message, is(
                "Appeal reference number: 12345678\n" +
                        "Appellant name: Jean Valjean\n" +
                        "Appellant NINO: JV123456\n" +
                        "\n" +
                        "The appellant has accepted the tribunal's view.\n" +
                        "\n" +
                        "someUrl\n" +
                        "\n" +
                        "PIP Benefit Appeals\n" +
                        "HMCTS\n"));
    }

    @Test
    public void buildDecisionRejected() {
        String reason = "some reason";
        String message = new EmailMessageBuilder().getDecisionRejectedMessage(caseDetails, reason, "someUrl");

        assertThat(message, is(
                "Appeal reference number: 12345678\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "The appellant has rejected the tribunal's view.\n" +
                "\n" +
                "Reasons for requesting a hearing:\n" +
                "\n" +
                reason + "\n" +
                "\n" +
                "someUrl\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }

    @Test
    public void buildAppellantStatement() {
        String message = new EmailMessageBuilder().getAppellantStatementMessage(caseDetails);

        assertThat(message, is(
                "Additional evidence submitted\n" +
                "\n" +
                "Appeal reference number: 12345678\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "The appellant has written a statement online and submitted it to the tribunal. Their statement is attached.\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }

    @Test
    public void buildEvidenceSubmitted() {
        String message = new EmailMessageBuilder().getEvidenceSubmittedMessage(caseDetails);
        String submittedDate = PdfDateUtil.reformatDate(LocalDate.now());

        assertThat(message, is(
                "Additional evidence submitted by appellant\n" +
                        "\n" +
                        "Appeal reference number: 12345678\n" +
                        "Appellant name: Jean Valjean\n" +
                        "Appellant NINO: JV123456\n" +
                        "\n" +
                        "Additional evidence was received by the tribunal for the above appeal on " +
                        submittedDate + ".\n" +
                        "\n" +
                        "PIP Benefit Appeals\n" +
                        "HMCTS\n"));
    }

    @Test
    public void buildQuestionEvidenceSubmitted() {
        QuestionSummary question = DataFixtures.someQuestionSummary();
        String questionHeaderText = question.getQuestionHeaderText();
        String message = new EmailMessageBuilder().getQuestionEvidenceSubmittedMessage(caseDetails, question);

        assertThat(message, is(
                "Additional evidence submitted in relation to question\n" +
                        "\n" +
                        "Appeal reference number: 12345678\n" +
                        "Appellant name: Jean Valjean\n" +
                        "Appellant NINO: JV123456\n" +
                        "\n" +
                        "Additional evidence was received by the tribunal for the above appeal on 08 August 2018. " +
                        "It was submitted in relation to the question " +
                        questionHeaderText +
                        "\n\n" +
                        "PIP Benefit Appeals\n" +
                        "HMCTS\n"));
    }
}