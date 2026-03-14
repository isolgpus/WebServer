package io.kiw.web.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncBlockingMapper<REQ, RES> {
    CompletableFuture<RES> handle(BlockingContext<REQ> ctx);
}
