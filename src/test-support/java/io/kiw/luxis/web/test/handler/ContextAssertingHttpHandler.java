package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

import static io.kiw.luxis.web.http.HttpResult.success;

public class ContextAssertingHttpHandler extends VertxJsonRoute<ContextRequest, ContextResponse, MyApplicationState> {

    private final ContextAsserter asserter;

    public ContextAssertingHttpHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public RequestPipeline<ContextResponse> handle(final HttpStream<ContextRequest, MyApplicationState> httpStream) {
        return httpStream
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return ctx.in().message;
            })
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in() + " blocking";
            })
            .asyncMap(ctx -> {
                asserter.assertInApplicationContext();
                return CompletableFuture.supplyAsync(() -> ctx.in() + " async");
            })
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in() + " map";
            })
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return ctx.in() + " blocking2";
            })
            .asyncMap(ctx -> {
                asserter.assertInApplicationContext();
                return CompletableFuture.supplyAsync(() -> ctx.in() + " async2");
            })

            .complete(ctx -> success(new ContextResponse(ctx.in())));
    }
}
