package io.kiw.web.test;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.jwt.*;
import io.kiw.web.cors.*;
import io.kiw.web.openapi.*;

import java.util.List;
import java.util.function.Consumer;

public interface TestWebSocketClient {
    void send(String jsonMessage);

    void onResponses(Consumer<List<String>> receivedMessageConsumer);

    void close();

    boolean isClosed();
}
