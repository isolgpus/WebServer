package io.kiw.web.test;

import io.kiw.web.infrastructure.WebSocketConnection;

import java.util.List;

public class StubWebSocketConnection implements WebSocketConnection {

    private final List<String> sentMessages;
    private boolean closed = false;

    public StubWebSocketConnection(List<String> sentMessages) {
        this.sentMessages = sentMessages;
    }

    @Override
    public void sendText(String text) {
        sentMessages.add(text);
    }

    @Override
    public void close() {
        closed = true;
    }


    public boolean isClosed() {
        return closed;
    }
}
