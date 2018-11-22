package uk.gov.hmcts.reform.sscscorbackend.service;

public class CaseNotCorException extends RuntimeException {
    public CaseNotCorException() {
        super("Case was found but it is not a COR case");
    }
}
