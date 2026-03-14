package io.kiw.web.infrastructure;

import java.util.Map;

public interface WebSocketConnection {

    void sendText(String text);

    void close();

    String pathParam(String key);

    String queryParam(String key);
}
