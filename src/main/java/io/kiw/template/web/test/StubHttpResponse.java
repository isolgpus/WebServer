package io.kiw.template.web.test;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class StubHttpResponse {
    public final String responseBody;
    private int statusCode = 200;
    private Map<String, String> responseHeaders =  new LinkedHashMap<>();
    private Map<String, String> responseCookies = new LinkedHashMap<>();

    public StubHttpResponse(String responseBody) {
        this.responseBody = responseBody;
    }

    public StubHttpResponse withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public static StubHttpResponse response(String expectedResponseBody) {
        return new StubHttpResponse(expectedResponseBody)
                .withHeader("Content-Type", "application/json");
    }


    @Override
    public String toString() {
        return "StubHttpResponse{" +
                "responseBody='" + responseBody + '\'' +
                ", statusCode=" + statusCode +
                ", responseHeaders=" + responseHeaders +
                ", responseCookies=" + responseCookies +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubHttpResponse that = (StubHttpResponse) o;
        return statusCode == that.statusCode &&
                Objects.equals(responseBody, that.responseBody) &&
                Objects.equals(responseHeaders, that.responseHeaders) &&
                Objects.equals(responseCookies, that.responseCookies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseBody, statusCode, responseHeaders, responseCookies);
    }

    public StubHttpResponse withHeader(String key, String value) {
        this.responseHeaders.put(key, value);
        return this;
    }

    public StubHttpResponse withCookie(String key, Cookie value) {
        this.responseCookies.put(key, value.getValue());
        return this;
    }
}
