package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

import static io.kiw.luxis.web.http.HttpResult.success;

public class AsyncMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .asyncMap(ctx -> CompletableFuture.supplyAsync(() -> ctx.in().value * 10))
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }
}
