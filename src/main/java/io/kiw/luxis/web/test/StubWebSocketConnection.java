package io.kiw.luxis.web.test;

import io.kiw.luxis.web.websocket.WebSocketConnection;

import java.util.List;

public class StubWebSocketConnection implements WebSocketConnection {

    private final List<String> sentMessages;
    private boolean closed = false;

    public StubWebSocketConnection(final List<String> sentMessages) {
        this.sentMessages = sentMessages;
    }

    @Override
    public void sendText(final String text) {
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
