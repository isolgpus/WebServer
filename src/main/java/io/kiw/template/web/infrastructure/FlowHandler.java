package io.kiw.template.web.infrastructure;

public interface FlowHandler<REQ, RES> {
    HttpResult<RES> handle(REQ request, HttpContext httpContext);
}
