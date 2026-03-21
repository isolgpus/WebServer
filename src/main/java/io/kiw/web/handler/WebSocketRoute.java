package io.kiw.web.handler;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.internal.ender.*;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class WebSocketRoute<IN, OUT, APP> extends TypeReference<IN> {

    public void onOpen(WebSocketSession<OUT> session, APP appState) {
    }

    public abstract WebSocketPipeline<OUT> onMessage(WebSocketStream<IN, APP> stream);

    public void onClose(WebSocketSession<OUT> session, APP appState) {
    }
}
