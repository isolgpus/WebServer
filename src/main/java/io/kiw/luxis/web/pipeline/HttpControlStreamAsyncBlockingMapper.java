package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.BlockingContext;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncBlockingMapper<REQ, RES> {
    CompletableFuture<RES> handle(BlockingContext<REQ> ctx);
}
