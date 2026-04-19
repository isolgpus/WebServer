package io.kiw.luxis.web.pipeline;

import io.vertx.core.Future;

@FunctionalInterface
public interface TransactionAsyncMapper<IN, OUT, APP, SESSION> {
    Future<OUT> handle(TransactionRouteContext<IN, APP, SESSION> ctx);
}
