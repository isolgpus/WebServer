package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.atomic.AtomicLong;

import static io.kiw.luxis.web.http.HttpResult.success;

public class CorrelatedAsyncBlockingMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    public static final AtomicLong capturedCorrelationId = new AtomicLong(-1);

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
