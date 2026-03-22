package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

import java.util.concurrent.CompletableFuture;

public class ContextAssertingWebSocketHandler extends WebSocketRoute<WebSocketEchoRequest, WebSocketEchoResponse, MyApplicationState> {

    private final ContextAsserter asserter;

    public ContextAssertingWebSocketHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public WebSocketPipeline<WebSocketEchoResponse> onMessage(final WebSocketStream<WebSocketEchoRequest, MyApplicationState> stream) {
        return stream
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return ctx.in().message;
            })
            .blockingMap(ctx -> {
                asserter.assertInWorkerContext();
                return ctx.in() + " blocked";
            })
            .map(ctx -> {
                asserter.assertInApplicationContext();
                return new WebSocketEchoResponse(ctx.in());
            })
            .complete();
    }
}
