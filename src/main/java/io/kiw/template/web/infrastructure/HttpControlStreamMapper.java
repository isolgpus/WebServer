package io.kiw.template.web.infrastructure;

public interface HttpControlStreamMapper<REQ, RES> {
    RES handle(REQ request, HttpContext httpContext);
}
