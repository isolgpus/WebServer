package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.BlockingContext;
import io.kiw.luxis.web.http.HttpErrorResponse;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncBlockingFlatMapper<REQ, RES> {
    CompletableFuture<Result<HttpErrorResponse, RES>> handle(BlockingContext<REQ> ctx);
}
