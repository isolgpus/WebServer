package io.kiw.web.internal;

import io.kiw.web.http.HttpBuffer;
import io.kiw.web.http.HttpCookie;
import io.kiw.web.http.RequestContext;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class VertxRequestContextImpl implements RequestContext {
    private final RoutingContext ctx;

    public VertxRequestContextImpl(RoutingContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public HttpCookie getRequestCookie(final String key) {
        Cookie cookie = this.ctx.request().getCookie(key);
        if (cookie == null) {
            return null;
        }
        return new HttpCookie(cookie.getName(), cookie.getValue());
    }

    @Override
    public String getQueryParam(final String key) {
        return this.ctx.request().getParam(key);
    }

    @Override
    public void addResponseHeader(String key, String value) {
        if (!hasEnded()) {
            this.ctx.response().putHeader(key, value);
        }
    }

    @Override
    public void addResponseCookie(HttpCookie value) {
        if (!hasEnded()) {
            this.ctx.response().addCookie(Cookie.cookie(value.name(), value.value()));
        }
    }

    @Override
    public String getRequestBody() {
        return this.ctx.getBodyAsString();
    }

    @Override
    public void setStatusCode(int statusCode) {
        if (!hasEnded()) {
            this.ctx.response().setStatusCode(statusCode);
        }
    }

    @Override
    public void end(String bodyResponse) {
        if (!hasEnded()) {
            this.ctx.end(bodyResponse);
        }
    }

    @Override
    public void end(HttpBuffer bodyResponse) {
        if (!hasEnded()) {
            this.ctx.end(Buffer.buffer(bodyResponse.bytes()));
        }
    }

    @Override
    public String getRequestHeader(String key) {
        return this.ctx.request().getHeader(key);
    }

    @Override
    public void next() {
        this.ctx.next();
    }

    @Override
    public void put(String key, Object successValue) {
        this.ctx.put(key, successValue);
    }

    @Override
    public Object get(String key) {
        return this.ctx.get(key);
    }

    @Override
    public boolean hasEnded() {
        return (boolean) this.ctx.data().getOrDefault("CONTEXT_DEAD", false);
    }

    @Override
    public String getPathParam(String key) {
        return this.ctx.pathParam(key);
    }

    @Override
    public void runOnContext(Runnable task) {
        this.ctx.vertx().runOnContext(v -> task.run());
    }

    @Override
    public Map<String, HttpBuffer> resolveUploadedFiles() {
        return this.ctx.fileUploads().stream()
            .collect(Collectors.toMap(
                FileUpload::fileName,
                a -> new HttpBuffer(ctx.vertx().fileSystem().readFileBlocking(a.uploadedFileName()).getBytes()),
                new BinaryOperator<HttpBuffer>() {
                    @Override
                    public HttpBuffer apply(HttpBuffer a, HttpBuffer b) {
                        return a;
                    }
                },
                LinkedHashMap::new));
    }
}
