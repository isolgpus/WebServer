package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.BlockingContext;
import io.kiw.luxis.web.http.HttpErrorResponse;

public interface HttpControlStreamBlockingFlatMapper<REQ, RES> {
    Result<HttpErrorResponse, RES> handle(BlockingContext<REQ> ctx);
}
