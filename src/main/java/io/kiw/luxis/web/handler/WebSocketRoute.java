package io.kiw.luxis.web.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.websocket.WebSocketSession;

public abstract class WebSocketRoute<IN, OUT, APP> extends TypeReference<IN> {

    public void onOpen(WebSocketSession<OUT> session, APP appState) {
    }

    public abstract WebSocketPipeline<OUT> onMessage(WebSocketStream<IN, APP> stream);

    public void onClose(WebSocketSession<OUT> session, APP appState) {
    }
}
