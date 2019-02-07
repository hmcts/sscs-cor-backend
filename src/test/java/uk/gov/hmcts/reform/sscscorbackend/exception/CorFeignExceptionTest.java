package uk.gov.hmcts.reform.sscscorbackend.exception;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import feign.Request;
import feign.Response;
import java.nio.charset.Charset;
import java.util.*;
import org.junit.Test;

public class CorFeignExceptionTest {
    @Test
    public void errorMessageContainsResponseWithoutBody() {
        HashMap<String, Collection<String>> headers = new HashMap<>();
        headers.put("header", singletonList("value1"));

        CorFeignException corFeignException = new CorFeignException("methodName", Response.builder()
                .status(200)
                .headers(headers)
                .request(Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), Request.Body.empty()))
                .build());

        assertThat(corFeignException.getMessage(), is(
                "methodName caused an error\n" +
                        "-----------------------------\n" +
                        "HTTP/1.1 200\n" +
                        "header: value1\n" +
                        "\n" +
                        "-----------------------------")
        );
    }

    @Test
    public void errorMessageContainsResponseWithMultipleHeaders() {
        HashMap<String, Collection<String>> headers = new HashMap<>();
        List<String> headerValues1 = new ArrayList<>();
        headerValues1.add("value1.1");
        headerValues1.add("value1.2");
        headers.put("header1", headerValues1);
        headers.put("header2", singletonList("value2.1"));

        CorFeignException corFeignException = new CorFeignException("methodName", Response.builder()
                .status(200)
                .headers(headers)
                .request(Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), Request.Body.empty()))
                .build());

        assertThat(corFeignException.getMessage(), is(
                "methodName caused an error\n" +
                        "-----------------------------\n" +
                        "HTTP/1.1 200\n" +
                        "header1: value1.1\n" +
                        "header1: value1.2\n" +
                        "header2: value2.1\n" +
                        "\n" +
                        "-----------------------------")
        );
    }

    @Test
    public void errorMessageContainsResponseWithBody() {
        HashMap<String, Collection<String>> headers = new HashMap<>();
        headers.put("header", singletonList("value1"));

        CorFeignException corFeignException = new CorFeignException("methodName", Response.builder()
                .status(200)
                .headers(headers)
                .request(Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), Request.Body.empty()))
                .body("some body", Charset.defaultCharset())
                .build());

        assertThat(corFeignException.getMessage(), is(
                "methodName caused an error\n" +
                        "-----------------------------\n" +
                        "HTTP/1.1 200\n" +
                        "header: value1\n" +
                        "\n" +
                        "some body\n" +
                        "-----------------------------")
        );
    }
}