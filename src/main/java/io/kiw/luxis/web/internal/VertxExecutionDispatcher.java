package io.kiw.luxis.web.internal;

import io.vertx.core.Vertx;

public class VertxExecutionDispatcher implements ExecutionDispatcher {
    private final Vertx vertx;

    public VertxExecutionDispatcher(final Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void handleBlocking(final Runnable o) {
        vertx.executeBlocking(() -> {
            o.run();
            return null;
        });
    }

    @Override
    public void handleOnEventLoop(final Runnable o) {
        vertx.runOnContext(unused -> o.run());
    }
}
