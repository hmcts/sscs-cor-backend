package uk.gov.hmcts.reform.sscscorbackend.controllers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HealthControllerTest {
    @Test
    public void alwaysReturnsOk() {
        ResponseEntity<String> health = new HealthController().getHealth();

        assertThat(health.getStatusCode(), is(HttpStatus.OK));
        assertThat(health.getBody(), containsString("UP"));
    }
}