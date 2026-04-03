package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.AsyncRouteContext;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncFlatMapper<REQ, RES, APP> {
    CompletableFuture<Result<HttpErrorResponse, RES>> handle(AsyncRouteContext<REQ, APP> ctx);
}
