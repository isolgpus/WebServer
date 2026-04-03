package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpErrorResponseException;
import io.kiw.luxis.web.http.client.CorrelatedAsync;
import io.kiw.luxis.web.http.client.LuxisAsync;

import java.util.concurrent.CompletableFuture;

public final class CorrelatedUtil {

    private CorrelatedUtil() {

    }

    public static <T> CorrelatedAsync<T> correlated(final PendingAsyncResponses pendingAsyncResponses) {
        final CompletableFuture<Result<HttpErrorResponse, T>> future = new CompletableFuture<>();
        final long correlationId = pendingAsyncResponses.register(future, 30_000);
        final LuxisAsync<T> luxisAsync = new LuxisAsync<>(future.thenApply(result -> result.fold(
                error -> {
                    throw new HttpErrorResponseException(error);
                },
                value -> value
        )));
        return new CorrelatedAsync<>(correlationId, luxisAsync);
    }
}
