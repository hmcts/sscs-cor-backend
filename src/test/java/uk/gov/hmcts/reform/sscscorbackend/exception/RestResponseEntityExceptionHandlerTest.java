package uk.gov.hmcts.reform.sscscorbackend.exception;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class RestResponseEntityExceptionHandlerTest {

    @Test
    public void logExceptionWithoutStackTrace() {
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> responseEntity = new RestResponseEntityExceptionHandler(false)
                .logExceptions(new RuntimeException("some exception"), request);

        assertThat(responseEntity.getBody().toString(), containsString("An error has occurred"));
        assertThat(responseEntity.getBody().toString(), not(containsString("java.lang.RuntimeException: some exception")));

    }

    @Test
    public void logExceptionWithStackTrace() {
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> responseEntity = new RestResponseEntityExceptionHandler(true)
                .logExceptions(new RuntimeException("some exception"), request);

        assertThat(responseEntity.getBody().toString(), containsString("An error has occurred"));
        assertThat(responseEntity.getBody().toString(), containsString("java.lang.RuntimeException: some exception"));
    }
}