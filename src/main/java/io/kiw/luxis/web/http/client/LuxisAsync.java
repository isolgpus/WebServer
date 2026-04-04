package io.kiw.luxis.web.http.client;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;

import java.util.concurrent.CompletableFuture;

public final class LuxisAsync<T> {
    private final CompletableFuture<Result<HttpErrorResponse, T>> future;

    public LuxisAsync(final CompletableFuture<Result<HttpErrorResponse, T>> future) {
        this.future = future;
    }

    public static <T> LuxisAsync<T> completed(final T value) {
        return new LuxisAsync<>(CompletableFuture.completedFuture(Result.success(value)));
    }

    public <R> LuxisAsync<R> map(final java.util.function.Function<T, R> mapper) {
        return new LuxisAsync<>(future.thenApply(result -> result.map(mapper)));
    }

    public CompletableFuture<Result<HttpErrorResponse, T>> toCompletableFuture() {
        return future;
    }

}
