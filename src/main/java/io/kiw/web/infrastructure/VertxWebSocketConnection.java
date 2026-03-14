package io.kiw.web.infrastructure;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;

public class VertxWebSocketConnection implements WebSocketConnection {

    private final ServerWebSocket webSocket;
    private final RoutingContext ctx;

    public VertxWebSocketConnection(ServerWebSocket webSocket, RoutingContext ctx) {
        this.webSocket = webSocket;
        this.ctx = ctx;
    }

    @Override
    public void sendText(String text) {
        webSocket.writeTextMessage(text);
    }

    @Override
    public void close() {
        webSocket.close();
    }

    @Override
    public String pathParam(String key) {
        return ctx.pathParam(key);
    }

    @Override
    public String queryParam(String key) {
        return ctx.request().getParam(key);
    }
}
