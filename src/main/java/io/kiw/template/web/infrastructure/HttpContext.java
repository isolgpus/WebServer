package io.kiw.template.web.infrastructure;

import io.vertx.core.http.Cookie;

public class HttpContext {
    final VertxContext ctx;

    public HttpContext(VertxContext ctx) {

        this.ctx = ctx;
    }

    public MapValidator getQueryParamValidator() {
        return ctx.getQueryParamValidator();
    }

    public MapValidator getRequestHeaderValidator() {
        return ctx.getRequestHeaderValidator();
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
