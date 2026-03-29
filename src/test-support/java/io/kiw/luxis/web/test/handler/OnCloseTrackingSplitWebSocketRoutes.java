package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.test.MyApplicationState;
import io.kiw.luxis.web.websocket.WebSocketSession;

public class OnCloseTrackingSplitWebSocketRoutes extends WebSocketRoutes<MyApplicationState> {

    @Override
    public void onOpen(final WebSocketSession session, final MyApplicationState appState) {

    }

    @Override
    public void registerRoutes(final WebSocketRoutesRegister<MyApplicationState> routesRegister) {
        routesRegister
            .route("echo", WebSocketEchoRequest.class, stream ->
                stream.map(ctx -> new WebSocketEchoResponse("echo: " + ctx.in().message))
                      .complete());
            
    }

    @Override
    public void onClose(final WebSocketSession session, final MyApplicationState appState) {

    }
}
