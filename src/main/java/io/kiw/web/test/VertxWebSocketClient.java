package io.kiw.web.test;

import io.vertx.core.http.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VertxWebSocketClient implements WebSocketClient {

    private final WebSocket webSocket;
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();
    private volatile boolean closed = false;

    VertxWebSocketClient(WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.textMessageHandler(receivedMessages::add);
        webSocket.closeHandler(v -> closed = true);
    }

    @Override
    public void send(String jsonMessage) {
        webSocket.writeTextMessage(jsonMessage);
    }

    @Override
    public List<String> received() {
        List<String> messages = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return messages;
    }

    @Override
    public void close() {
        webSocket.close();
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
