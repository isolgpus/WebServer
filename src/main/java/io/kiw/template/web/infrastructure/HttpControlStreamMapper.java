package io.kiw.template.web.infrastructure;

public interface HttpControlStreamMapper<REQ, RES, APP> {
    RES handle(REQ request, HttpContext httpContext, APP application);
}
