package io.kiw.template.web.infrastructure;

public interface HttpControlStreamBlockingMapper<REQ, RES> {
    RES handle(REQ request, HttpContext context);
}
