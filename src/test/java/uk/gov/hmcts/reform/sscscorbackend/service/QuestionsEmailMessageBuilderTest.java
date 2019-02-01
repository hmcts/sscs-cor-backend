package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

public class QuestionsEmailMessageBuilderTest {

    @Test
    public void buildContent() {
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

        String message = new QuestionsEmailMessageBuilder().getMessage(caseDetails);

        assertThat(message, is("Appellant: Jean Valjean\n\n" +
                "Appeal reference number: caseReference\n\n" +
                "National Insurance number: JV123456\n\n" +
                "The tribunal have sent some questions to the appellant in the above appeal.\n" +
                "Please see the questions attached.\n\n" +
                "PIP Benefit Appeals\n" +
                "Her Majesty's Courts and Tribunal Service\n"));
    }
}