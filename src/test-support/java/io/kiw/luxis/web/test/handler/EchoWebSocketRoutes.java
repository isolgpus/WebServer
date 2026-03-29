package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.internal.IndividualMessageWebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketHandler;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class EchoWebSocketRoutes extends WebSocketRoutes<MyApplicationState> {

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister
            .route("echo", WebSocketEchoRequest.class, stream ->
                stream.map(ctx -> "echo: " + ctx.in().message)
                .map(ctx -> new WebSocketEchoResponse(ctx.in()))
                .complete());
            
    }
}
