package io.kiw.luxis.web.websocket;

public interface WebSocketConnection {

    void sendText(String text);

    void close();
}
