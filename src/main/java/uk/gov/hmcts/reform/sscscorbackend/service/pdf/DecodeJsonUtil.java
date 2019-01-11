package uk.gov.hmcts.reform.sscscorbackend.service.pdf;

public class DecodeJsonUtil {
    private DecodeJsonUtil() {}

    public static String decodeStringWithWhitespace(String value) {
        if (value != null) {
            return value.replaceAll("\\\\n", "\n");
        }
        return null;
    }
}
