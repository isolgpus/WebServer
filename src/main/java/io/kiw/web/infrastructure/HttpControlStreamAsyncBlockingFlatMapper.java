package io.kiw.web.infrastructure;

import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncBlockingFlatMapper<REQ, RES> {
    CompletableFuture<Result<HttpErrorResponse, RES>> handle(BlockingContext<REQ> ctx);
}
