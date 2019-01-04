package uk.gov.hmcts.reform.sscscorbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Service;

@Service
public class I18nBuilder {
    public HashMap build() throws IOException {
        InputStream in = getClass().getResourceAsStream("/json/en.json");
        return new ObjectMapper().readValue(IOUtils.toByteArray(in), HashMap.class);
    }
}
