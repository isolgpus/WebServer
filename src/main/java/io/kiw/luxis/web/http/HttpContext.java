package io.kiw.luxis.web.http;

import io.kiw.luxis.web.jwt.JwtClaims;

import java.util.Map;

public class HttpContext {
    public final RequestContext ctx;

    public HttpContext(RequestContext ctx) {
        this.ctx = ctx;
    }

    public String getQueryParam(String key) {
        return ctx.getQueryParam(key);
    }

    public String getRequestHeader(String key) {
        return ctx.getRequestHeader(key);
    }

    public HttpCookie getRequestCookie(String key) {
        return ctx.getRequestCookie(key);
    }

    public void addResponseHeader(String key, String value) {
        ctx.addResponseHeader(key, value);
    }

    public void addResponseCookie(HttpCookie value) {
        ctx.addResponseCookie(value);
    }

    public Map<String, HttpBuffer> resolveUploadedFiles() {
        return this.ctx.resolveUploadedFiles();
    }

    public String getPathParam(String key) {
        return ctx.getPathParam(key);
    }

    public JwtClaims getJwtClaims() {
        return (JwtClaims) ctx.get("__jwt_claims__");
    }
}
