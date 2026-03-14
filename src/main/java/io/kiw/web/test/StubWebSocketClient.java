package io.kiw.web.test;

import io.kiw.web.infrastructure.WebSocketRouteHandler;
import io.kiw.web.infrastructure.WebSocketSession;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StubWebSocketClient {

    private final List<String> receivedMessages = new ArrayList<>();
    private final WebSocketRouteHandler<?, ?, ?> handler;
    private final WebSocketSession<?> session;
    private final StubWebSocketConnection connection;

    StubWebSocketClient(WebSocketRouteHandler<?, ?, ?> handler, Map<String, String> pathParams, Map<String, String> queryParams) {
        this.handler = handler;
        this.connection = new StubWebSocketConnection(receivedMessages, pathParams, queryParams);
        this.session = handler.createSession(connection);
        handler.onOpen(session);
    }

    public void send(String jsonMessage) {
        handler.onMessage(jsonMessage, session);
    }

    public List<String> received() {
        List<String> messages = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return messages;
    }

    public void close() {
        handler.onClose(session);
    }

    public boolean isClosed() {
        return connection.isClosed();
    }
}
