package io.kiw.luxis.web.websocket;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.internal.*;

public interface WebSocketConnection {

    void sendText(String text);

    void close();
}
