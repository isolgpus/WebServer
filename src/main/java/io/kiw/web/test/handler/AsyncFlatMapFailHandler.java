package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncFlatMapFailHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
            .<AsyncMapResponse>asyncFlatMap(ctx -> CompletableFuture.completedFuture(HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async flat map failed"))))
            .complete(ctx -> HttpResult.success(ctx.in()));
    }
}
