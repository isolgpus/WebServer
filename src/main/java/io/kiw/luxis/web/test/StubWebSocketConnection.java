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

import io.kiw.luxis.web.websocket.WebSocketConnection;

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
