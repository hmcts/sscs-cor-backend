package uk.gov.hmcts.reform.sscscorbackend.service.pdf.util;

import static java.time.LocalDate.parse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscscorbackend.service.pdf.util.PdfDateUtil.reformatDate;

import org.junit.Test;

public class PdfDateUtilTest {
    @Test
    public void canHandleBlankDate() {
        String reformatDate = reformatDate("");

        assertThat(reformatDate, is(""));
    }

    @Test
    public void formatsStringDate() {
        String reformatDate = reformatDate("2001-06-24");

        assertThat(reformatDate, is("24 June 2001"));
    }

    @Test
    public void formatsLocalDate() {
        String reformatDate = reformatDate(parse("2001-06-24"));

        assertThat(reformatDate, is("24 June 2001"));
    }
}