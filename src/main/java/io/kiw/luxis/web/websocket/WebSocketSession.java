package io.kiw.luxis.web.websocket;

import tools.jackson.databind.ObjectMapper;

public class WebSocketSession {

    private final WebSocketConnection connection;
    private final ObjectMapper objectMapper;

    public WebSocketSession(final WebSocketConnection connection, final ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    public WebSocketConnection connection() {
        return connection;
    }

    public void send(final Object message) {
        connection.sendText(objectMapper.writeValueAsString(message));
    }

    public void close() {
        connection.close();
    }

}
