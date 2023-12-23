package io.kiw.web.infrastructure;

public interface HttpControlStreamBlockingMapper<REQ, RES> {
    RES handle(REQ request, HttpContext context);
}
