package uk.gov.hmcts.reform.sscscorbackend.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class EvidenceTest {
    @Test
    public void getId() {
        Evidence evidence = new Evidence("http://example.com/documents/someId", "someFIleName", "someDate");
        String id = evidence.getId();
        
        assertThat(id, is("someId"));
    }
}