package io.kiw.web.websocket;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.internal.*;

public interface WebSocketConnection {

    void sendText(String text);

    void close();
}
