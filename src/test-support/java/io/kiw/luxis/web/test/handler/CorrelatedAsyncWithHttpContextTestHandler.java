package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class CorrelatedAsyncWithHttpContextTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .map(ctx -> {
                    final String multiplier = ctx.http().getQueryParam("multiplier");
                    return multiplier != null ? Integer.parseInt(multiplier) : 1;
                })
                .correlatedAsyncMap(Integer.class, ctx -> {
                    ctx.app().setPendingCorrelationId(ctx.correlationId());
                    ctx.app().setPendingValue(ctx.in());
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }
}
