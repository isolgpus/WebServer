package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.vertx.core.http.ServerWebSocket;

public class VertxWebSocketConnection implements WebSocketConnection {

    private final ServerWebSocket webSocket;

    public VertxWebSocketConnection(final ServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void sendText(final String text) {
        webSocket.writeTextMessage(text);
    }

    @Override
    public void close() {
        webSocket.close();
    }

}
