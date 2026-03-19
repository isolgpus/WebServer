package io.kiw.web.infrastructure;

import io.vertx.core.Vertx;

public class WebSocketRouterWrapperImpl implements WebSocketRouterWrapper {
    private final Vertx vertx;

    public WebSocketRouterWrapperImpl(Vertx vertx) {
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
