package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.VertxContext;
import io.vertx.core.http.Cookie;

import java.util.LinkedHashMap;
import java.util.Map;

public class StubVertxContext implements VertxContext {
    private String requestBody;
    private final Map<String, String> queryParams;
    private final Map<String, String> requestHeaders;
    private final Map<String, Cookie> requestCookies;
    private final Map<String, String> responseHeaders = new LinkedHashMap<>();
    private final Map<String, Cookie> responseCookies = new LinkedHashMap<>();
    private String responseBody;
    private int statusCode = 200;
    private Map<String, Object> state = new LinkedHashMap<>();
    private boolean finished = false;

    public StubVertxContext(String requestBody, Map<String, String> queryParams, Map<String, String> requestHeaders, Map<String, Cookie> requestCookies) {

        this.requestBody = requestBody;
        this.queryParams = queryParams;
        this.requestHeaders = requestHeaders;
        this.requestCookies = requestCookies;
    }


    @Override
    public Cookie getRequestCookie(String key) {
        return this.requestCookies.get(key);
    }

    @Override
    public String getQueryParam(String key) {
        return this.queryParams.get(key);
    }

    @Override
    public void addResponseHeader(String key, String value) {
        this.responseHeaders.put(key, value);
    }

    @Override
    public void addResponseCookie(Cookie value) {
        this.responseCookies.put(value.getName(), value);
    }

    @Override
    public String getRequestBody() {
        return this.requestBody;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public void end(String responseBody) {
        this.responseBody = responseBody;
        this.finished = true;
    }

    @Override
    public String getRequestHeader(String key) {
        return this.requestHeaders.get(key);
    }

    @Override
    public void next() {

    }

    @Override
    public void put(String key, Object successValue) {
        this.state.put(key, successValue);
    }

    @Override
    public Object get(String key) {
        return this.state.get(key);
    }

    public StubHttpResponse getResponse() {
        StubHttpResponse stubHttpResponse = new StubHttpResponse(responseBody).withStatusCode(statusCode);
        for (Map.Entry<String, String> header : this.responseHeaders.entrySet()) {
            stubHttpResponse.withHeader(header.getKey(), header.getValue());
        }

        for (Map.Entry<String, Cookie> cookie : this.responseCookies.entrySet()) {
            stubHttpResponse.withCookie(cookie.getValue());
        }
        return stubHttpResponse;
    }

    public boolean hasFinished() {
        return finished;
    }
}
