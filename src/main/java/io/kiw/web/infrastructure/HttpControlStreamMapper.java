package io.kiw.web.infrastructure;

public interface HttpControlStreamMapper<REQ, RES, APP> {
    RES handle(REQ request, HttpContext httpContext, APP application);
}
