package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

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
