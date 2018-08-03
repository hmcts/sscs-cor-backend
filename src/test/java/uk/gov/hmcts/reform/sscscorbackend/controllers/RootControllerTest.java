package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class RootControllerTest {
    @Test
    public void checkResponse() {
        ResponseEntity<String> response = new RootController().welcome();

        assertThat(response.getBody(), is("Root"));
    }
}