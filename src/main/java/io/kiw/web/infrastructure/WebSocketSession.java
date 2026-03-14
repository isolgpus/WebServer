package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketSession<OUT> {

    private final WebSocketConnection connection;
    private final ObjectMapper objectMapper;

    public WebSocketSession(WebSocketConnection connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    public void send(OUT message) {
        try {
            connection.sendText(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        connection.close();
    }

    public String pathParam(String key) {
        return connection.pathParam(key);
    }

    public String queryParam(String key) {
        return connection.queryParam(key);
    }
}
