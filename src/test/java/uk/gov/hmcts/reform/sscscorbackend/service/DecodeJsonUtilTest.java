package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.sscscorbackend.service.pdf.DecodeJsonUtil;

public class DecodeJsonUtilTest {
    @Test
    public void decodesStringWithNewLine() {
        String decoded = DecodeJsonUtil.decodeStringWithWhitespace("A \\n new line string");

        assertThat(decoded, is("A \n new line string"));
    }

    @Test
    public void handleNUllValue() {
        String decoded = DecodeJsonUtil.decodeStringWithWhitespace(null);

        assertThat(decoded, is(nullValue()));
    }
}