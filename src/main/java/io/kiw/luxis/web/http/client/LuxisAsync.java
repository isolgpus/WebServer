package io.kiw.luxis.web.http.client;

import java.util.concurrent.CompletableFuture;

public final class LuxisAsync<T> {
    private final CompletableFuture<T> future;

    public LuxisAsync(final CompletableFuture<T> future) {
        this.future = future;
    }

    public static <T> LuxisAsync<T> completed(final T value) {
        return new LuxisAsync<>(CompletableFuture.completedFuture(value));
    }

    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }

}
