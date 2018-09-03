package uk.gov.hmcts.reform.sscscorbackend.exception;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = getLogger(RestResponseEntityExceptionHandler.class);

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> logExceptions(Exception ex, WebRequest request) {
        SscsCorBackendException exc = new SscsCorBackendException(AlertLevel.P3, ex);
        LOG.error("Unhandled exception", exc);

        return handleExceptionInternal(
                ex,
                "An error has occurred",
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }
}