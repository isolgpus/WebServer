package io.kiw.template.web.infrastructure;

public interface HttpControlStreamBlockingFlatMapper<REQ, RES> {
    HttpResult<RES> handle(REQ request, HttpContext httpContext);
}
