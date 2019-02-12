package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

public class DwpEmailMessageBuilderTest {

    private DwpEmailMessageBuilder dwpEmailMessageBuilder;

    @Before
    public void setUp() throws Exception {
        dwpEmailMessageBuilder = new DwpEmailMessageBuilder();
    }

    @Test
    public void buildAnswersContent() {
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
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
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
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
                        .build()
                )
                .build();

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
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
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
}