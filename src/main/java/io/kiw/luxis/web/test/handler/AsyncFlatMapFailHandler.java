package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncFlatMapFailHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
            .<AsyncMapResponse>asyncFlatMap(ctx -> CompletableFuture.completedFuture(HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async flat map failed"))))
            .complete(ctx -> HttpResult.success(ctx.in()));
    }
}
