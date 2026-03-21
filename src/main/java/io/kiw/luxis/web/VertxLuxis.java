package io.kiw.luxis.web;

import io.vertx.core.Vertx;

import java.util.function.BiConsumer;

public class VertxLuxis<APP> implements Luxis<APP> {
    private final Vertx vertx;
    private final APP applicationState;

    public VertxLuxis(final Vertx vertx, final APP applicationState) {
        this.vertx = vertx;
        this.applicationState = applicationState;
    }


    @Override
    public <IN> void apply(final IN immutableState, final BiConsumer<IN, APP> applicationStateConsumer) {
        vertx.runOnContext((v) -> applicationStateConsumer.accept(immutableState, applicationState));
    }

    @Override
    public void close() {
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }
}
