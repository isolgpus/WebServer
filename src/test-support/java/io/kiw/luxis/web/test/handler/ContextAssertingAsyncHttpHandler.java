package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class ContextAssertingAsyncHttpHandler extends JsonHandler<ContextRequest, ContextResponse, MyApplicationState> {

    private final ContextAsserter asserter;
    private Luxis<?> luxis;

    public ContextAssertingAsyncHttpHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public RequestPipeline<ContextResponse> handle(final HttpStream<ContextRequest, MyApplicationState> httpStream) {
        return httpStream
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in().message;
            })
            .<String>asyncMap(ctx -> {
                asserter.assertInApplicationContext();
                luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in() + " async"));
            })
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return ctx.in() + " blocking";
            })
            .<String>asyncBlockingMap(ctx -> {
                asserter.assertInWorkerContext();
                luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in() + " asyncBlocking"));
            })
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return new ContextResponse(ctx.in() + " map");
            })
            .complete(ctx -> success(ctx.in()));
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
