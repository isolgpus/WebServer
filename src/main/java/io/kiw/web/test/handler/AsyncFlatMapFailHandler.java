package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncFlatMapFailHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(HttpResponseStream<AsyncMapRequest, MyApplicationState> httpResponseStream) {
        return httpResponseStream
            .asyncFlatMap(ctx -> CompletableFuture.completedFuture(HttpResult.error(400, new ErrorMessageResponse("async flat map failed"))))
            .complete(ctx -> HttpResult.success(ctx.in()));
    }
}
