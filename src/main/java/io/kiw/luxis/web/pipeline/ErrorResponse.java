package io.kiw.luxis.web.pipeline;

public record ErrorResponse(String message) implements CorruptWebSocketInputStrategy {
}
