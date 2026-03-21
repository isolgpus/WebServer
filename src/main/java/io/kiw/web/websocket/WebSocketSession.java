package io.kiw.web.websocket;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.internal.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketSession<OUT> {

    private final WebSocketConnection connection;
    private final ObjectMapper objectMapper;

    public WebSocketSession(WebSocketConnection connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    public WebSocketConnection connection() {
        return connection;
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

}
