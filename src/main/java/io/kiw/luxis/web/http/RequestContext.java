package io.kiw.luxis.web.http;

import java.util.Map;

public interface RequestContext {
    HttpCookie getRequestCookie(String key);

    String getQueryParam(String key);

    void addResponseHeader(String key, String value);

    void addResponseCookie(HttpCookie value);

    String getRequestBody();

    void setStatusCode(int statusCode);

    void end(String bodyResponse);

    void end(HttpBuffer bodyResponse);

    String getRequestHeader(String key);

    void next();

    void put(String key, Object successValue);

    Object get(String key);

    boolean hasEnded();

    Map<String, HttpBuffer> resolveUploadedFiles();

    String getPathParam(String key);

    void runOnContext(Runnable task);
}
