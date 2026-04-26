package io.kiw.luxis.web.http.client;

import io.kiw.luxis.result.Result;

import java.util.concurrent.CompletableFuture;

public final class LuxisAsync<T, ERR> {
    private final CompletableFuture<Result<ERR, T>> future;

    public LuxisAsync(final CompletableFuture<Result<ERR, T>> future) {
        this.future = future;
    }

    public static <T, ERR> LuxisAsync<T, ERR> completed(final T value) {
        return new LuxisAsync<>(CompletableFuture.completedFuture(Result.success(value)));
    }

    public static <T, ERR> LuxisAsync<T, ERR> failed(final Throwable throwable) {
        final CompletableFuture<Result<ERR, T>> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return new LuxisAsync<>(future);
    }
    public static <T, ERR> LuxisAsync<T, ERR> failed(final ERR failure) {
        return new LuxisAsync<>(CompletableFuture.completedFuture(Result.error(failure)));
    }

    public <R> LuxisAsync<R, ERR> map(final java.util.function.Function<T, R> mapper) {
        return new LuxisAsync<>(future.thenApply(result -> result.map(mapper)));
    }

    public CompletableFuture<Result<ERR, T>> toCompletableFuture() {
        return future;
    }

}
