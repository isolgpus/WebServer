package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.AsyncMapConfigBuilder;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class WebSocketCustomTimeoutHandler extends WebSocketRoute<WebSocketNumberRequest, MyApplicationState> {

    private Runnable onRegistered = () -> {};

    public WebSocketCustomTimeoutHandler() {
    }

    public void setOnRegistered(final Runnable onRegistered) {
        this.onRegistered = onRegistered;
    }

    @Override
    public WebSocketPipeline onMessage(final WebSocketSplitStream<WebSocketNumberRequest, MyApplicationState> stream) {
        return stream
            .on("number", WebSocketNumberRequest.class, s ->
                s.<Integer>correlatedAsyncMap(ctx -> {
                    // Deliberately do NOT call handleAsyncResponse — simulates missing response
                    onRegistered.run();
                }, new AsyncMapConfigBuilder().setTimeoutMillis(1_000).build())
                .map(ctx -> new WebSocketNumberResponse(ctx.in()))
                .complete())
            .build();
    }
}
