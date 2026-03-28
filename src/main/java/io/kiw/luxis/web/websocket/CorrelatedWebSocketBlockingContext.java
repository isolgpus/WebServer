package io.kiw.luxis.web.websocket;

public record CorrelatedWebSocketBlockingContext<IN>(long correlationId, IN in) {
}
