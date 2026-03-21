package io.kiw.luxis.web.test;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.cors.*;
import io.kiw.luxis.web.openapi.*;

import io.kiw.luxis.web.internal.WebSocketRouteHandler;
import io.kiw.luxis.web.websocket.WebSocketSession;

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
