package uk.gov.hmcts.reform.sscscorbackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private final boolean enableDebugMessage;

    public RestResponseEntityExceptionHandler(@Value("${enable_debug_error_message}")boolean enableDebugMessage) {
        this.enableDebugMessage = enableDebugMessage;
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> logExceptions(Exception ex, WebRequest request) {
        SscsCorBackendException exc = new SscsCorBackendException(ex);
        log.error("Unhandled exception", exc);

        String body = "An error has occurred" +
                (enableDebugMessage ? "\n\n" + ExceptionUtils.getStackTrace(ex) : "");
        return handleExceptionInternal(
                ex,
                body,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }
}