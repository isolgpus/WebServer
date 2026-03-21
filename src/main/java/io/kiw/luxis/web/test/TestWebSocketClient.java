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

import java.util.List;
import java.util.function.Consumer;

public interface TestWebSocketClient {
    void send(String jsonMessage);

    void onResponses(Consumer<List<String>> receivedMessageConsumer);

    void close();

    boolean isClosed();
}
