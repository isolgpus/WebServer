package io.kiw.template.web.infrastructure;

public interface HttpControlStreamFlatMapper<REQ, RES> {
    HttpResult<RES> handle(REQ request, HttpContext httpContext);
}
