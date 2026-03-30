package io.kiw.luxis.web.http.client;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    public <U> LuxisAsync<U> map(final Function<T, U> fn) {
        return new LuxisAsync<>(future.thenApply(fn));
    }

    public <U> LuxisAsync<U> flatMap(final Function<T, LuxisAsync<U>> fn) {
        return new LuxisAsync<>(future.thenCompose(value -> fn.apply(value).toCompletableFuture()));
    }
}
