package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.RouteContext;

public interface HttpControlStreamFlatMapper<REQ, RES, APP> {
    Result<HttpErrorResponse, RES> handle(RouteContext<REQ, APP> ctx);
}
