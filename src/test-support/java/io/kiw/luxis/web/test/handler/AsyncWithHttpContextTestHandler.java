package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class AsyncWithHttpContextTestHandler extends JsonHandler<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {


    private Luxis<?> luxis;
    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .map(ctx -> {
                    final String multiplier = ctx.http().getQueryParam("multiplier");
                    return multiplier != null ? Integer.parseInt(multiplier) : 1;
                })
                .<Integer>asyncMap(ctx -> {
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in() * 7));
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
