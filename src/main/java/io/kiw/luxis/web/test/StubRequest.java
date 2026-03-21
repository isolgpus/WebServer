package io.kiw.luxis.web.test;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;
import io.kiw.luxis.web.openapi.*;

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
