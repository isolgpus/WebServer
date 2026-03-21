package io.kiw.luxis.web.test;

import io.kiw.luxis.web.http.HttpBuffer;
import io.kiw.luxis.web.http.HttpCookie;
import io.kiw.luxis.web.http.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class StubRequestContext implements RequestContext {
    private String requestBody;
    private final Map<String, String> queryParams;
    private final Map<String, String> requestHeaders;
    private final Map<String, HttpCookie> requestCookies;
    private final Map<String, HttpBuffer> fileUploads;
    private Map<String, String> pathParams = new LinkedHashMap<>();
    private final Map<String, String> responseHeaders = new LinkedHashMap<>();
    private final Map<String, HttpCookie> responseCookies = new LinkedHashMap<>();
    private String responseBody;
    private int statusCode = 200;
    private Map<String, Object> state = new LinkedHashMap<>();
    private boolean finished = false;

    public StubRequestContext(String requestBody, Map<String, String> queryParams, Map<String, String> requestHeaders, Map<String, HttpCookie> requestCookies, Map<String, HttpBuffer> fileUploads) {
        this.requestBody = requestBody;
        this.queryParams = queryParams;
        this.requestHeaders = requestHeaders;
        this.requestCookies = requestCookies;
        this.fileUploads = fileUploads;
    }

    @Override
    public HttpCookie getRequestCookie(String key) {
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
    public void addResponseCookie(HttpCookie value) {
        this.responseCookies.put(value.name(), value);
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
        if (statusCode == 204) {
            this.responseBody = "";
        } else {
            this.responseBody = responseBody;
        }
        this.finished = true;
    }

    @Override
    public void end(HttpBuffer bodyResponse) {
        if (statusCode == 204) {
            this.responseBody = "";
        } else {
            this.responseBody = bodyResponse.toString(StandardCharsets.UTF_8);
        }
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

    @Override
    public boolean hasEnded() {
        return false;
    }

    @Override
    public String getPathParam(String key) {
        return this.pathParams.get(key);
    }

    @Override
    public Map<String, HttpBuffer> resolveUploadedFiles() {
        return fileUploads;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public TestHttpResponse getResponse() {
        TestHttpResponse testHttpResponse = new TestHttpResponse(responseBody).withStatusCode(statusCode);
        for (Map.Entry<String, String> header : this.responseHeaders.entrySet()) {
            testHttpResponse.withHeader(header.getKey(), header.getValue());
        }
        for (Map.Entry<String, HttpCookie> cookie : this.responseCookies.entrySet()) {
            testHttpResponse.withCookie(cookie.getValue());
        }
        return testHttpResponse;
    }

    public boolean hasFinished() {
        return finished;
    }

    @Override
    public void runOnContext(Runnable task) {
        task.run();
    }
}
