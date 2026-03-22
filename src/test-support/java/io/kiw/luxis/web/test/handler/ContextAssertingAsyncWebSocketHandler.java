package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class ContextAssertingAsyncWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    private final ContextAsserter asserter;

    public ContextAssertingAsyncWebSocketHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return ctx.in().message;
            })
            .asyncMap(ctx -> {
                asserter.assertInApplicationContext();
                return CompletableFuture.supplyAsync(() -> {
                    return ctx.in() + " async";
                });
            })
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in() + "map";
            })
            .asyncMap(ctx -> {
                asserter.assertInApplicationContext();
                return CompletableFuture.supplyAsync(() -> {
                    return ctx.in() + " async2";
                });
            })
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return new WebSocketEchoResponse(ctx.in() + " blocking");
            })
            .complete();
    }
}
