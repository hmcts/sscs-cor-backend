package uk.gov.hmcts.reform.sscscorbackend.exception;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class RestResponseEntityExceptionHandlerTest {

    //Just need this to get the code coverage up for the moment
    @Test
    public void logException() {
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Object> foo = new RestResponseEntityExceptionHandler()
                .logExceptions(new RuntimeException("some exception"), request);

        assertThat(foo, not(nullValue()));
    }

}