package io.kiw.template.web.test;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class StubRequest {
    public final String path;
    public String body;
    public Map<String, String> queryParams = new LinkedHashMap<>();
    public Map<String, String> headers = new LinkedHashMap<>();
    public Map<String, Cookie> cookies = new LinkedHashMap<>();
    public Map<String, Buffer> fileUploads = new LinkedHashMap<>();

    public StubRequest(String path) {
        this.path = path;
    }

    public static StubRequest request(String path)
    {
        return new StubRequest(path);
    }

    public StubRequest body(String body) {
        this.body = body;
        return this;
    }

    public StubRequest queryParam(String key, String value) {
        this.queryParams.put(key, value);
        return this;
    }

    public StubRequest headerParam(String key, String value) {
        this.headers.put(key,value);
        return this;
    }

    public StubRequest cookie(String key, String value) {
        this.cookies.put(key, new CookieImpl(key, value));
        return this;
    }

    public StubRequest fileUpload(String name, String contents) {
        BufferImpl buffer = new BufferImpl();
        buffer.appendString(contents, StandardCharsets.UTF_8.displayName());
        this.fileUploads.put(name, buffer);
        return this;
    }
}
