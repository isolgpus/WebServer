package io.kiw.luxis.web.websocket;

public record WebSocketContext<IN, APP, RESP>(IN in, WebSocketSession<RESP> connection, APP app) {
}
