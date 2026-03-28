package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class ContextAssertingAsyncBlockingHttpHandler extends VertxJsonRoute<ContextRequest, ContextResponse, MyApplicationState> {

    private final ContextAsserter asserter;

    public ContextAssertingAsyncBlockingHttpHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public RequestPipeline<ContextResponse> handle(final HttpStream<ContextRequest, MyApplicationState> httpStream) {
        return httpStream
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in().message;
            })
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return ctx.in() + " blocking";
            })
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in() + " map";
            })
            .complete(ctx -> success(new ContextResponse(ctx.in())));
    }
}
