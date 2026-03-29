package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.ContextAsserter;
import io.kiw.luxis.web.test.MyApplicationState;

public class ContextAssertingWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    private final ContextAsserter asserter;

    public ContextAssertingWebSocketHandler(final ContextAsserter asserter) {
        this.asserter = asserter;
    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketRoutesRegister<MyApplicationState> stream) {
        return stream
            .route("echo", WebSocketEchoRequest.class, s ->
                s.map(ctx -> {
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
                .complete())
            .build();
    }
}
