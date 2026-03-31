package io.kiw.luxis.web;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpErrorResponseException;
import io.kiw.luxis.web.http.client.CorrelatedAsync;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.VertxExecutionDispatcher;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class VertxLuxis<APP> implements Luxis<APP> {
    private final VertxExecutionDispatcher executionDispatcher;
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;
    private final AutoCloseable onClose;

    public VertxLuxis(final VertxExecutionDispatcher executionDispatcher, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final AutoCloseable onClose) {
        this.executionDispatcher = executionDispatcher;
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.onClose = onClose;
    }


    @Override
    public <IN> void apply(final IN immutableState, final BiConsumer<IN, APP> applicationStateConsumer) {
        executionDispatcher.handleOnApplicationContext(() -> applicationStateConsumer.accept(immutableState, applicationState));
    }

    @Override
    public <T> void handleAsyncResponse(final long correlationId, final Result<HttpErrorResponse, T> result) {
        pendingAsyncResponses.complete(correlationId, result);
        // triggers RouterWrapper.handleAsync
    }

    @Override
    public <T> CorrelatedAsync<T> createCorrelatedAsync() {
        return createCorrelatedAsync(30_000);
    }

    @Override
    public <T> CorrelatedAsync<T> createCorrelatedAsync(final long timeoutMillis) {
        final CompletableFuture<Result<HttpErrorResponse, T>> future = new CompletableFuture<>();
        final long correlationId = pendingAsyncResponses.register(future, timeoutMillis);
        final LuxisAsync<T> luxisAsync = new LuxisAsync<>(future.thenApply(result -> result.fold(
                error -> {
                    throw new HttpErrorResponseException(error);
                },
                value -> value
        )));
        return new CorrelatedAsync<>(correlationId, luxisAsync);
    }

    @Override
    public void close() throws Exception {
        onClose.close();
    }
}
