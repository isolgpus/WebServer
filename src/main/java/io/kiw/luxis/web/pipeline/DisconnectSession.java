package io.kiw.luxis.web.pipeline;

public record DisconnectSession() implements CorruptWebSocketInputStrategy {
    public static final DisconnectSession INSTANCE = new DisconnectSession();
}
