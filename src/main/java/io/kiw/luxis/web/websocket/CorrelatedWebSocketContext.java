package io.kiw.luxis.web.websocket;

public record CorrelatedWebSocketContext<IN, APP>(long correlationId, IN in, WebSocketConnection connection, APP app) {
}
