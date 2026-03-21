package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import io.vertx.core.Vertx;

public class VertxWebSocketRouterWrapperImpl implements WebSocketRouterWrapper {
    private final Vertx vertx;

    public VertxWebSocketRouterWrapperImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void handleBlocking(Runnable o) {
        vertx.executeBlocking(() -> {
            o.run();
            return null;
        });
    }

    @Override
    public void handleOnEventLoop(Runnable o) {
        vertx.runOnContext(unused -> o.run());
    }
}
