package io.kiw.web.infrastructure;

import io.kiw.result.Result;

public interface HttpControlStreamFlatMapper<REQ, RES, APP> {
    Result<HttpErrorResponse, RES> handle(REQ request, HttpContext context, APP app);
}
