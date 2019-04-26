package uk.gov.hmcts.reform.sscscorbackend.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;

public class JuiUrlGeneratorTest {
    @Test
    public void generateUrl() {
        String baseUrl = "http://baseUrl";
        long caseId = 123456L;
        String url = new JuiUrlGenerator(baseUrl).generateUrl(SscsCaseDetails.builder().id(caseId).build());

        assertThat(url, is("http://baseUrl/case/SSCS/Benefit/123456/summary"));
    }

}