package uk.gov.hmcts.reform.sscscorbackend.exception;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

public class SscsCorBackendException extends UnknownErrorCodeException {
    public SscsCorBackendException(AlertLevel alertLevel, Throwable cause) {
        super(alertLevel, cause);
    }
}
