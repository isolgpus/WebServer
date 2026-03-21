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

import io.kiw.web.websocket.WebSocketConnection;

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
