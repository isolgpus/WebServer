package io.kiw.web.test;

import java.util.List;

public interface WebSocketClient {
    void send(String jsonMessage);

    List<String> received();

    void close();

    boolean isClosed();
}
