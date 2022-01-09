package io.kiw.template.web.infrastructure;

import io.vertx.core.http.Cookie;

public class HttpContext {
    final VertxContext ctx;

    public HttpContext(VertxContext ctx) {

        this.ctx = ctx;
    }

    public String getQueryParam(String key) {
        return ctx.getQueryParam(key);
    }

    public MapValidator getQueryParamValidator() {
        return ctx.getQueryParamValidator();
    }

    public String getRequestHeader(String key) {
        return ctx.getRequestHeader(key);
    }

    public Cookie getRequestCookie(String key) {
        return ctx.getRequestCookie(key);
    }

    public void addResponseHeader(String key, String value) {
        ctx.addResponseHeader(key, value);
    }

    public void addResponseCookie(Cookie value) {
        ctx.addResponseCookie(value);
    }
}
