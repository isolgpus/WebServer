package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.Luxis;
import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

public class ContextAssertingAsyncWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    private final ContextAsserter asserter;
    private Luxis<?> luxis;

    public ContextAssertingAsyncWebSocketHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketRoutesRegister<MyApplicationState> stream) {
        return stream
            .route("echo", WebSocketEchoRequest.class, s ->
                s.blockingMap(ctx -> {
                    asserter.assertInWorkerContext();
                    return ctx.in().message;
                })
                .<String>asyncMap(ctx -> {
                    asserter.assertInApplicationContext();
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in() + " async"));
                })
                .map(ctx -> {
                    asserter.assertInApplicationContext();
                    return ctx.in() + "map";
                })
                .<String>asyncMap(ctx -> {
                    asserter.assertInApplicationContext();
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in() + " async2"));
                })
                .<String>asyncBlockingMap(ctx -> {
                    asserter.assertInWorkerContext();
                    luxis.handleAsyncResponse(ctx.correlationId(), Result.success(ctx.in() + " async3"));
                })
                .blockingMap(ctx -> {
                    asserter.assertInWorkerContext();
                    return new WebSocketEchoResponse(ctx.in() + " blocking");
                })
                .complete())
            .build();
    }

    public void evillyReferenceLuxis(Luxis<?> luxis) {
        this.luxis = luxis;
    }
}
