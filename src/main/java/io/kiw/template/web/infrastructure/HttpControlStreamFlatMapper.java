package io.kiw.template.web.infrastructure;

public interface HttpControlStreamFlatMapper<REQ, RES, APP> {
    HttpResult<RES> handle(REQ request, HttpContext httpContext, APP applicationState);
}
