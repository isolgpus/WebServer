package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

import io.kiw.web.internal.WebSocketRouteHandler;
import io.kiw.web.websocket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StubTestWebSocketClient implements TestWebSocketClient {

    private final List<String> receivedMessages = new ArrayList<>();
    private final WebSocketRouteHandler<?, ?, ?> handler;
    private final WebSocketSession<?> session;
    private final StubWebSocketConnection connection;

    StubTestWebSocketClient(WebSocketRouteHandler<?, ?, ?> handler) {
        this.handler = handler;
        this.connection = new StubWebSocketConnection(receivedMessages);
        this.session = handler.createSession(connection);
        handler.onOpen(session);
    }

    @Override
    public void send(String jsonMessage) {
        handler.onMessage(jsonMessage, session);
    }


    @Override
    public void onResponses(Consumer<List<String>> receivedMessageConsumer) {
        receivedMessageConsumer.accept(receivedMessages);
        receivedMessages.clear();
    }

    @Override
    public void close() {
        handler.onClose(session);
        connection.close();
    }

    @Override
    public boolean isClosed() {
        return connection.isClosed();
    }
}
