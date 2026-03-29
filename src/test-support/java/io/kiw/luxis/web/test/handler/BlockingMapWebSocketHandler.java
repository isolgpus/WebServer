package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;

public class BlockingMapWebSocketHandler extends WebSocketRoutes<MyApplicationState> {

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister
            .route("number", WebSocketNumberRequest.class, s ->
                s.map(ctx -> ctx.in().value)
                 .blockingMap(ctx -> ctx.in() * 2)
                 .map(ctx -> new WebSocketNumberResponse(ctx.in()))
                 .complete());
            
    }
}
