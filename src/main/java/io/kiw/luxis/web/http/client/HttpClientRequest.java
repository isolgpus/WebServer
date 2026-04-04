package io.kiw.luxis.web.http.client;

import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpClientRequest {
    private final String url;
    private String body;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();

    private HttpClientRequest(final String url) {
        this.url = url;
    }

    public static HttpClientRequest request(final String url) {
        return new HttpClientRequest(url);
    }

    public HttpClientRequest body(final String body) {
        this.body = body;
        return this;
    }

    public HttpClientRequest jsonBody(final Object body, final ObjectMapper objectMapper) {
        this.body = objectMapper.writeValueAsString(body);
        return this;
    }

    public HttpClientRequest header(final String key, final String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpClientRequest queryParam(final String key, final String value) {
        this.queryParams.put(key, value);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}
