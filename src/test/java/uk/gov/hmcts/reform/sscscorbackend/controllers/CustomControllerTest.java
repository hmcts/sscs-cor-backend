package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class CustomControllerTest {
    @Test
    public void checkResponse() {
        String response = new CustomController().custom();

        assertThat(response, is("custom"));
    }
}