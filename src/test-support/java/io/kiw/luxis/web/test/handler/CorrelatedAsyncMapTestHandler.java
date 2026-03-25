package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class CorrelatedAsyncMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .correlatedAsyncMap(Integer.class, ctx -> {
                    ctx.app().setPendingCorrelationId(ctx.correlationId());
                    ctx.app().setPendingValue(ctx.in().value);
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }
}
