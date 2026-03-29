package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.AsyncMapConfigBuilder;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;

public class WebSocketCustomTimeoutRoutes extends WebSocketRoutes<MyApplicationState> {

    private Runnable onRegistered = () -> {};

    public WebSocketCustomTimeoutRoutes() {
    }

    public void setOnRegistered(final Runnable onRegistered) {
        this.onRegistered = onRegistered;
    }

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister
            .route("number", WebSocketNumberRequest.class, s ->
                s.<Integer>asyncMap(ctx -> {
                    // Deliberately do NOT call handleAsyncResponse — simulates missing response
                    onRegistered.run();
                }, new AsyncMapConfigBuilder().setTimeoutMillis(1_000).build())
                .map(ctx -> new WebSocketNumberResponse(ctx.in()))
                .complete());
            
    }
}
