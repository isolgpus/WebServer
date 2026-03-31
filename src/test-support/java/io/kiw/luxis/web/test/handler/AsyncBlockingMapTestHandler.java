package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.client.CorrelatedAsync;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class AsyncBlockingMapTestHandler extends JsonHandler<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    private Luxis<?> luxis;

    public AsyncBlockingMapTestHandler() {
    }

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .<Integer>asyncBlockingMap(ctx -> {
                    final CorrelatedAsync<Integer> correlated = luxis.createCorrelatedAsync();
                    luxis.handleAsyncResponse(correlated.correlationId(), Result.success(ctx.in().value * 20));
                    return correlated.async();
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
