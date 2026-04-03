package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.BlockingRouteContext;
import io.kiw.luxis.web.http.HttpErrorResponse;

public interface HttpControlStreamBlockingFlatMapper<REQ, RES> {
    Result<HttpErrorResponse, RES> handle(BlockingRouteContext<REQ> ctx);
}
