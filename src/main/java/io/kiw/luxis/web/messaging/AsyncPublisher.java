package io.kiw.luxis.web.messaging;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.vertx.core.Future;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public final class AsyncPublisher<ERR> {

    private final Publisher publisher;

    public AsyncPublisher(final Publisher publisher) {
        this.publisher = publisher;
    }

    public LuxisAsync<Void, ERR> publish(final String key, final String message) {
        require();
        return wrap(publisher.publish(key, message));
    }

    public LuxisAsync<Void, ERR> publish(final String key, final byte[] message) {
        require();
        return wrap(publisher.publish(key, message));
    }

    public LuxisAsync<Void, ERR> publish(final String key, final ByteBuffer message) {
        require();
        return wrap(publisher.publish(key, message));
    }

    private void require() {
        if (publisher == null) {
            throw new IllegalStateException(
                    "Cannot publish — no Publisher registered at Luxis.start(...) / Luxis.test(...).");
        }
    }

    private static <ERR> LuxisAsync<Void, ERR> wrap(final Future<Void> future) {
        final CompletableFuture<Result<ERR, Void>> cf = new CompletableFuture<>();
        future.onComplete(ar -> {
            if (ar.succeeded()) {
                cf.complete(Result.success(ar.result()));
            } else {
                cf.completeExceptionally(ar.cause());
            }
        });
        return new LuxisAsync<>(cf);
    }
}
