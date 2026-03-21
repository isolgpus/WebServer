package io.kiw.luxis.web.test;

import io.kiw.luxis.web.http.HttpBuffer;
import io.kiw.luxis.web.http.HttpCookie;

import java.util.LinkedHashMap;
import java.util.Map;

public class StubRequest {
    public final String path;
    public String body;
    public Map<String, String> queryParams = new LinkedHashMap<>();
    public Map<String, String> headers = new LinkedHashMap<>();
    public Map<String, HttpCookie> cookies = new LinkedHashMap<>();
    public Map<String, HttpBuffer> fileUploads = new LinkedHashMap<>();

    public StubRequest(String path) {
        this.path = path;
    }

    public static StubRequest request(String path) {
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
        this.headers.put(key, value);
        return this;
    }

    public StubRequest cookie(String key, String value) {
        this.cookies.put(key, new HttpCookie(key, value));
        return this;
    }

    public StubRequest fileUpload(String name, String contents) {
        this.fileUploads.put(name, HttpBuffer.fromString(contents));
        return this;
    }
}
