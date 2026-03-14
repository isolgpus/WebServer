package io.kiw.web.infrastructure;

import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncFlatMapper<REQ, RES, APP> {
    CompletableFuture<Result<HttpErrorResponse, RES>> handle(RouteContext<REQ, APP> ctx);
}
