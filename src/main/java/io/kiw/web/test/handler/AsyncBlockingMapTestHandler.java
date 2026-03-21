package io.kiw.web.test.handler;

import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.handler.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

import static io.kiw.web.http.HttpResult.success;

public class AsyncBlockingMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .asyncBlockingMap(ctx -> CompletableFuture.supplyAsync(() -> ctx.in().value * 20))
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }
}
