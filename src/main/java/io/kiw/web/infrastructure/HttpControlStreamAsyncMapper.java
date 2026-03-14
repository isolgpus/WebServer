package io.kiw.web.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncMapper<REQ, RES, APP> {
    CompletableFuture<RES> handle(RouteContext<REQ, APP> ctx);
}
