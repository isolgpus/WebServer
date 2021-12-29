package io.kiw.template.web.infrastructure;

import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

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
        this.ctx.response().putHeader(key, value);
    }

    @Override
    public void addResponseCookie(Cookie value) {
        this.ctx.response().addCookie(value);
    }

    @Override
    public String getRequestBody() {
        return this.ctx.getBodyAsString();
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.ctx.response().setStatusCode(statusCode);
    }

    @Override
    public void end(String bodyResponse) {
        this.ctx.end(bodyResponse);
    }

    @Override
    public String getRequestHeader(String key) {
        return this.ctx.request().getHeader(key);
    }
}
