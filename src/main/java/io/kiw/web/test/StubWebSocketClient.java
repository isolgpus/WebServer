package io.kiw.web.test;

import io.kiw.web.infrastructure.WebSocketRouteHandler;
import io.kiw.web.infrastructure.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StubWebSocketClient implements WebSocketClient {

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

    @Override
    public void send(String jsonMessage) {
        handler.onMessage(jsonMessage, session);
    }

    @Override
    public List<String> received() {
        List<String> messages = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return messages;
    }

    @Override
    public void close() {
        handler.onClose(session);
    }

    @Override
    public boolean isClosed() {
        return connection.isClosed();
    }
}
