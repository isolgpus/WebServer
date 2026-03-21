package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class AsyncFlatMapFailHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
            .<AsyncMapResponse>asyncFlatMap(ctx -> CompletableFuture.completedFuture(HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("async flat map failed"))))
            .complete(ctx -> HttpResult.success(ctx.in()));
    }
}
