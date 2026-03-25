package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.atomic.AtomicLong;

import static io.kiw.luxis.web.http.HttpResult.success;

public class CorrelatedAsyncBlockingMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    private final AtomicLong capturedCorrelationId;

    public CorrelatedAsyncBlockingMapTestHandler(final AtomicLong capturedCorrelationId) {
        this.capturedCorrelationId = capturedCorrelationId;
    }

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .correlatedAsyncBlockingMap(Integer.class, ctx -> {
                    capturedCorrelationId.set(ctx.correlationId());
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }
}
