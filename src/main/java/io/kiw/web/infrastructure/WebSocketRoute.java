package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class WebSocketRoute<IN, OUT, APP> extends TypeReference<IN> {

    public void onOpen(WebSocketSession<OUT> session, APP appState) {
    }

    public abstract void onMessage(IN message, WebSocketSession<OUT> session, APP appState);

    public void onClose(WebSocketSession<OUT> session, APP appState) {
    }
}
