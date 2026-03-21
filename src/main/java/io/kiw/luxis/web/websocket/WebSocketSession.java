package io.kiw.luxis.web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketSession<OUT> {

    private final WebSocketConnection connection;
    private final ObjectMapper objectMapper;

    public WebSocketSession(final WebSocketConnection connection, final ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    public WebSocketConnection connection() {
        return connection;
    }

    public void send(final OUT message) {
        try {
            connection.sendText(objectMapper.writeValueAsString(message));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        connection.close();
    }

}
