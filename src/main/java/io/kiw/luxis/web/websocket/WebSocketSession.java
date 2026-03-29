package io.kiw.luxis.web.websocket;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebSocketSession<RESP> {

    private final WebSocketConnection connection;
    private final ObjectMapper objectMapper;
    private final Map<Class<?>, String> classToTypeKey;

    public WebSocketSession(final WebSocketConnection connection, final ObjectMapper objectMapper, final Map<Class<?>, String> classToTypeKey) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.classToTypeKey = classToTypeKey;
    }

    public void send(final RESP message) {
        final String typeKey = classToTypeKey.get(message.getClass());
        if (typeKey == null) {
            throw new IllegalArgumentException("Unregistered response type: " + message.getClass().getName());
        }
        final WebSocketResponseMessage envelope = new WebSocketResponseMessage(typeKey, message);
        connection.sendText(objectMapper.writeValueAsString(envelope));
    }

    public CompletableFuture<Void> sendRaw(final String message) {
        return connection.sendText(message);
    }

    public void close() {
        connection.close();
    }

}
