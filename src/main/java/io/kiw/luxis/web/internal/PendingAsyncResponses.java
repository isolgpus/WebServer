package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PendingAsyncResponses {
    private final AtomicLong nextId = new AtomicLong(0);
    private final ConcurrentHashMap<Long, CompletableFuture<Result<HttpErrorResponse, ?>>> pending = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> long register(final CompletableFuture<Result<HttpErrorResponse, T>> future) {
        final long id = nextId.getAndIncrement();
        pending.put(id, (CompletableFuture<Result<HttpErrorResponse, ?>>) (CompletableFuture<?>) future);
        return id;
    }

    @SuppressWarnings("unchecked")
    public <T> void complete(final long correlationId, final Result<HttpErrorResponse, T> result) {
        final CompletableFuture<Result<HttpErrorResponse, ?>> future = pending.remove(correlationId);
        if (future == null) {
            throw new IllegalArgumentException("No pending async response for correlationId: " + correlationId);
        }
        future.complete((Result<HttpErrorResponse, ?>) (Result<?, ?>) result);
    }
}
