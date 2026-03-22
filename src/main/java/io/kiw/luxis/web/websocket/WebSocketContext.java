package io.kiw.luxis.web.websocket;

public record WebSocketContext<IN, APP>(IN in, WebSocketConnection connection, APP app) {
}
