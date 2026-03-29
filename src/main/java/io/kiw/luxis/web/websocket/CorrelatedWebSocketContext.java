package io.kiw.luxis.web.websocket;

public record CorrelatedWebSocketContext<IN, APP, RESP>(long correlationId, IN in, WebSocketSession<RESP> connection,
                                                        APP app) {
}
