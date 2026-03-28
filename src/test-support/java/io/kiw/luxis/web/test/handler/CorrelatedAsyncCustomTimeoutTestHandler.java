package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.AsyncMapConfig;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class CorrelatedAsyncCustomTimeoutTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    private final AsyncMapConfig config;
    private Runnable onRegistered = () -> {};

    public CorrelatedAsyncCustomTimeoutTestHandler(final AsyncMapConfig config) {
        this.config = config;
    }

    public void setOnRegistered(final Runnable onRegistered) {
        this.onRegistered = onRegistered;
    }

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .<Integer>correlatedAsyncMap(ctx -> {
                    // Deliberately do NOT call handleAsyncResponse — simulates missing response
                    onRegistered.run();
                }, config)
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> HttpResult.success(ctx.in()));
    }
}
