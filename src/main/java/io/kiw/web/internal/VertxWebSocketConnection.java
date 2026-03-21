package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import io.vertx.core.http.ServerWebSocket;

public class VertxWebSocketConnection implements WebSocketConnection {

    private final ServerWebSocket webSocket;

    public VertxWebSocketConnection(ServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void sendText(String text) {
        webSocket.writeTextMessage(text);
    }

    @Override
    public void close() {
        webSocket.close();
    }

}
