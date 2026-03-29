package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.websocket.WebSocketSession;

public abstract class WebSocketRoute<APP> {

    public void onOpen(final WebSocketSession session, final APP appState) {
    }

    public abstract WebSocketPipeline onMessage(final WebSocketSplitStream<APP> splitStream);

    public void onClose(final WebSocketSession session, final APP appState) {
    }
}
