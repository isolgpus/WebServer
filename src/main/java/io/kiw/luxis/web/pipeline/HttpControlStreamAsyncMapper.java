package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.internal.RouteContext;

import java.util.concurrent.CompletableFuture;

public interface HttpControlStreamAsyncMapper<REQ, RES, APP> {
    CompletableFuture<RES> handle(RouteContext<REQ, APP> ctx);
}
