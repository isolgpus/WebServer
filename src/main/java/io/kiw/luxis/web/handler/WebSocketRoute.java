package io.kiw.luxis.web.handler;

import tools.jackson.core.type.TypeReference;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.websocket.WebSocketSession;

public abstract class WebSocketRoute<IN, OUT, APP> extends TypeReference<IN> {

    public void onOpen(final WebSocketSession<OUT> session, final APP appState) {
    }

    public abstract WebSocketPipeline<OUT> onMessage(final WebSocketStream<IN, APP> stream);

    public void onClose(final WebSocketSession<OUT> session, final APP appState) {
    }
}
