package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.HttpStream;
import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

import static io.kiw.web.infrastructure.HttpResult.success;

public class AsyncMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .asyncMap(ctx -> CompletableFuture.supplyAsync(() -> ctx.in().value * 10))
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }
}
