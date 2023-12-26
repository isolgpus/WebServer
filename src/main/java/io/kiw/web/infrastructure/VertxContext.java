package io.kiw.web.infrastructure;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;

import java.util.Map;

public interface VertxContext {
    Cookie getRequestCookie(String key);

    String getQueryParam(String key);

    void addResponseHeader(String key, String value);

    void addResponseCookie(Cookie value);

    String getRequestBody();

    void setStatusCode(int statusCode);

    void end(String bodyResponse);

    void end(Buffer bodyResponse);

    String getRequestHeader(String key);

    void next();

    void put(String key, Object successValue);

    Object get(String key);

    boolean hasEnded();

    Map<String, Buffer> resolveUploadedFiles();

}
