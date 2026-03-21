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

import io.vertx.core.http.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class VertxTestWebSocketClient implements TestWebSocketClient {

    private final WebSocket webSocket;
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();
    private volatile boolean closed = false;

    VertxTestWebSocketClient(WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.textMessageHandler(receivedMessages::add);
        webSocket.closeHandler(v -> closed = true);
    }

    @Override
    public void send(String jsonMessage) {
        webSocket.writeTextMessage(jsonMessage);
    }

    @Override
    public void onResponses(Consumer<List<String>> receivedMessageConsumer) {
        long deadline = System.currentTimeMillis() + 1000;
        Throwable latestException = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                receivedMessageConsumer.accept(receivedMessages);
                receivedMessages.clear();
                return;
            } catch (RuntimeException | AssertionError e) {
                latestException = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }

        throw new RuntimeException(latestException);
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
