package io.kiw.luxis.web.test;

import io.vertx.core.http.WebSocket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class VertxTestWebSocketClient implements TestWebSocketClient {

    private final WebSocket webSocket;
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();
    private volatile boolean closed = false;

    VertxTestWebSocketClient(final WebSocket webSocket) {
        this.webSocket = webSocket;
        webSocket.textMessageHandler(receivedMessages::add);
        webSocket.closeHandler(v -> closed = true);
    }

    @Override
    public void send(final String jsonMessage) {
        webSocket.writeTextMessage(jsonMessage);
    }

    @Override
    public void onResponses(final Consumer<List<String>> receivedMessageConsumer) {
        final long deadline = System.currentTimeMillis() + 1000;
        Throwable latestException = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                receivedMessageConsumer.accept(receivedMessages);
                receivedMessages.clear();
                return;
            } catch (final RuntimeException | AssertionError e) {
                latestException = e;
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException ie) {
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
