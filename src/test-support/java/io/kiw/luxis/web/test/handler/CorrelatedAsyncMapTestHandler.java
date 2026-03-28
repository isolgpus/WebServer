package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.function.Function;

import static io.kiw.luxis.web.http.HttpResult.success;

public class CorrelatedAsyncMapTestHandler extends VertxJsonRoute<AsyncMapRequest, AsyncMapResponse, MyApplicationState> {

    private final Function<Integer, Result<HttpErrorResponse, Integer>> responder;
    private Luxis<?> luxis;

    public CorrelatedAsyncMapTestHandler() {
        this(value -> Result.success(value * 10));
    }

    public CorrelatedAsyncMapTestHandler(final Function<Integer, Result<HttpErrorResponse, Integer>> responder) {
        this.responder = responder;
    }

    @Override
    public RequestPipeline<AsyncMapResponse> handle(final HttpStream<AsyncMapRequest, MyApplicationState> httpStream) {
        return httpStream
                .correlatedAsyncMap(Integer.class, ctx -> {
                    luxis.handleAsyncResponse(ctx.correlationId(), responder.apply(ctx.in().value));
                })
                .map(ctx -> new AsyncMapResponse(ctx.in()))
                .complete(ctx -> success(ctx.in()));
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
