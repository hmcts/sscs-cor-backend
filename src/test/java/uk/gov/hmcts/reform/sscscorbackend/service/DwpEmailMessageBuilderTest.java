package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

public class DwpEmailMessageBuilderTest {

    private DwpEmailMessageBuilder dwpEmailMessageBuilder;
    private SscsCaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        dwpEmailMessageBuilder = new DwpEmailMessageBuilder();
        caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .caseReference("caseReference")
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
        String message = dwpEmailMessageBuilder.getAnswerMessage(caseDetails);

        assertThat(message, is("Appeal reference number: caseReference\n" +
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
        String message = new DwpEmailMessageBuilder().getQuestionMessage(caseDetails);

        assertThat(message, is("Appeal reference number: caseReference\n" +
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
        String message = new DwpEmailMessageBuilder().getRelistedMessage(caseDetails);

        assertThat(message, is("Hearing required\n" +
                "\n" +
                "Appeal reference number: caseReference\n" +
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
        String message = new DwpEmailMessageBuilder().getDecisionIssuedMessage(caseDetails);

        assertThat(message, is("Preliminary view offered\n" +
                "\n" +
                "Appeal reference number: caseReference\n" +
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
        String message = new DwpEmailMessageBuilder().getDecisionAcceptedMessage(caseDetails);

        assertThat(message, is(
                "Appeal reference number: caseReference\n" +
                        "Appellant name: Jean Valjean\n" +
                        "Appellant NINO: JV123456\n" +
                        "\n" +
                        "The appellant has accepted the tribunal's view.\n" +
                        "\n" +
                        "PIP Benefit Appeals\n" +
                        "HMCTS\n"));
    }

    @Test
    public void buildDecisionRejected() {
        String reason = "some reason";
        String message = new DwpEmailMessageBuilder().getDecisionRejectedMessage(caseDetails, reason);

        assertThat(message, is(
                "Appeal reference number: caseReference\n" +
                "Appellant name: Jean Valjean\n" +
                "Appellant NINO: JV123456\n" +
                "\n" +
                "The appellant has rejected the tribunal's view.\n" +
                "\n" +
                "Reasons for requesting a hearing:\n" +
                "\n" +
                reason + "\n" +
                "\n" +
                "PIP Benefit Appeals\n" +
                "HMCTS\n"));
    }
}