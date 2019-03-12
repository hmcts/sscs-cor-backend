package uk.gov.hmcts.reform.sscscorbackend.exception;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CorErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return new CorFeignException(methodKey, response);
    }
}
