package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.websocket.WebSocketSession;

public abstract class WebSocketRoutes<APP> {

    public void onOpen(final WebSocketSession session, final APP appState) {
    }

    public abstract WebSocketPipeline onMessage(final WebSocketRoutesRegister<APP> splitStream);

    public void onClose(final WebSocketSession session, final APP appState) {
    }
}
