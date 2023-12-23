package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public interface HttpControlStreamBlockingFlatMapper<REQ, RES> {
    Result<HttpErrorResponse, RES> handle(REQ request, HttpContext context);
}
