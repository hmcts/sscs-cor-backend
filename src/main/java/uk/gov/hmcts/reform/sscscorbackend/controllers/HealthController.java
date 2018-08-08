package uk.gov.hmcts.reform.sscscorbackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @RequestMapping(method = RequestMethod.GET, value = "/health", produces = "application/json")
    public ResponseEntity<String> getHealth() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }
}
