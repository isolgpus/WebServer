package io.kiw.luxis.web.handler;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.internal.ender.*;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class WebSocketRoute<IN, OUT, APP> extends TypeReference<IN> {

    public void onOpen(WebSocketSession<OUT> session, APP appState) {
    }

    public abstract WebSocketPipeline<OUT> onMessage(WebSocketStream<IN, APP> stream);

    public void onClose(WebSocketSession<OUT> session, APP appState) {
    }
}
