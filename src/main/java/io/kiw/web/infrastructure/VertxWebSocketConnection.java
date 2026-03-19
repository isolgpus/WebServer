package io.kiw.web.infrastructure;

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
