package io.kiw.web.infrastructure;

public interface WebSocketConnection {

    void sendText(String text);

    void close();
}
