package uk.gov.hmcts.reform.sscscorbackend.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import org.junit.Test;

public class I18nBuilderTest {
    @Test
    public void canLoadI18n() throws IOException {
        HashMap i18n = new I18nBuilder().build();

        assertThat(i18n.containsKey("tribunalView"), is(true));
    }

}