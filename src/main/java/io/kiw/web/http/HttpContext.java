package io.kiw.web.http;

import io.kiw.web.jwt.JwtClaims;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;

import java.util.Map;

public class HttpContext {
    public final VertxContext ctx;

    public HttpContext(VertxContext ctx) {

        this.ctx = ctx;
    }

    public String getQueryParam(String key) {
        return ctx.getQueryParam(key);
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

    public Map<String, Buffer> resolveUploadedFiles() {
        return this.ctx.resolveUploadedFiles();
    }

    public String getPathParam(String key) {
        return ctx.getPathParam(key);
    }

    public JwtClaims getJwtClaims() {
        return (JwtClaims) ctx.get("__jwt_claims__");
    }
}
