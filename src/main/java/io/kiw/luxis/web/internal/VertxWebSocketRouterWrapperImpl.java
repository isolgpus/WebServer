package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

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
