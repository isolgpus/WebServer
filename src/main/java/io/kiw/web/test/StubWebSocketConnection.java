package io.kiw.web.test;

import io.kiw.web.infrastructure.WebSocketConnection;

import java.util.List;
import java.util.Map;

public class StubWebSocketConnection implements WebSocketConnection {

    private final List<String> sentMessages;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;
    private boolean closed = false;

    public StubWebSocketConnection(List<String> sentMessages, Map<String, String> pathParams, Map<String, String> queryParams) {
        this.sentMessages = sentMessages;
        this.pathParams = pathParams;
        this.queryParams = queryParams;
    }

    @Override
    public void sendText(String text) {
        sentMessages.add(text);
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public String pathParam(String key) {
        return pathParams.get(key);
    }

    @Override
    public String queryParam(String key) {
        return queryParams.get(key);
    }

    public boolean isClosed() {
        return closed;
    }
}
