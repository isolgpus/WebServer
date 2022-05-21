package io.kiw.template.web.infrastructure;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.stream.Collectors;

public class VertxContextImpl implements VertxContext {
    private final RoutingContext ctx;

    public VertxContextImpl(RoutingContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Cookie getRequestCookie(final String key)
    {
        return this.ctx.request().getCookie(key);
    }

    @Override
    public String getQueryParam(final String key)
    {
        return this.ctx.request().getParam(key);
    }

    @Override
    public void addResponseHeader(String key, String value) {
        if(!hasEnded())
        {
            this.ctx.response().putHeader(key, value);
        }
    }

    @Override
    public void addResponseCookie(Cookie value) {
        if(!hasEnded()) {
            this.ctx.response().addCookie(value);
        }
    }

    @Override
    public String getRequestBody() {
        return this.ctx.getBodyAsString();
    }

    @Override
    public void setStatusCode(int statusCode) {
        if(!hasEnded())
        {
            this.ctx.response().setStatusCode(statusCode);
        }
    }

    @Override
    public void end(String bodyResponse) {
        if(!hasEnded()) {
            this.ctx.end(bodyResponse);
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
        return (boolean)this.ctx.data().getOrDefault("CONTEXT_DEAD", false);
    }


    @Override
    public Map<String, Buffer> resolveUploadedFiles() {
        return this.ctx.fileUploads().stream()
            .collect(Collectors.toMap(FileUpload::fileName, a -> ctx.vertx().fileSystem().readFileBlocking(a.uploadedFileName())));
    }
}
