package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.HttpContext;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.ValidationResultMapper;
import io.kiw.template.web.test.MyApplicationState;

import java.util.Optional;

public class EchoHelper  {



    public static <T> HttpResult<EchoRequestWithQueryParams<T>> mapQueryParams(T echoRequest, HttpContext httpContext, MyApplicationState myApplicationState) {
        return httpContext.getQueryParamValidator()
                        .optional("queryExample").next()
                        .toHttpResult((ValidationResultMapper<Optional<String>, EchoRequestWithQueryParams<T>>) queryExample -> new EchoRequestWithQueryParams<>(echoRequest, queryExample.orElse(null)));
    }


    public static <T> HttpResult<EchoRequestWithQueryParamAndHeader<T>> mapHeaders(EchoRequestWithQueryParams<T> echoRequest, HttpContext httpContext, MyApplicationState myApplicationState) {
        return httpContext.getRequestHeaderValidator()
                .optional("requestHeaderExample").next()
                .toHttpResult((ValidationResultMapper<Optional<String>, EchoRequestWithQueryParamAndHeader<T>>) requestHeaderExample ->
                        new EchoRequestWithQueryParamAndHeader<>(echoRequest.echoRequest, echoRequest.queryExample, requestHeaderExample.orElse(null)));
    }


    public static class EchoRequestWithQueryParams<T> {
        final T echoRequest;
        final String queryExample;

        public EchoRequestWithQueryParams(T echoRequest, String queryExample) {
            this.echoRequest = echoRequest;
            this.queryExample = queryExample;
        }
    }

    public static class EchoRequestWithQueryParamAndHeader<T> extends EchoRequestWithQueryParams<T> {
        final String requestHeaderExample;

        public EchoRequestWithQueryParamAndHeader(T echoRequest, String queryExample, String requestHeaderExample) {
            super(echoRequest, queryExample);
            this.requestHeaderExample = requestHeaderExample;
        }
    }


}
