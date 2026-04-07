package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.LuxisMapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.ScheduleType;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class LuxisStream<IN, APP, ERR> {
    protected final List<LuxisMapInstruction<ERR>> instructionChain;
    protected final APP applicationState;
    protected final PendingAsyncResponses pendingAsyncResponses;

    protected LuxisStream(final List<LuxisMapInstruction<ERR>> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    protected abstract void onInstructionAdded(LuxisMapInstruction<ERR> instruction);

    protected abstract Function<HttpErrorResponse, ERR> asyncErrorMapper();

    protected abstract <OUT> CompletableFuture<Result<ERR, OUT>> handleAsyncException(
            CompletableFuture<Result<ERR, OUT>> future);

    protected void addSyncInstruction(final boolean isBlocking, final LuxisMapInstruction.SyncHandler<ERR> handler, final boolean lastStep) {
        final LuxisMapInstruction<ERR> instruction = new LuxisMapInstruction<>(isBlocking, handler, lastStep);
        onInstructionAdded(instruction);
        instructionChain.add(instruction);
    }

    protected void addAsyncInstruction(final boolean isBlocking, final LuxisMapInstruction.AsyncHandler<ERR> handler, final boolean lastStep) {
        final LuxisMapInstruction<ERR> instruction = new LuxisMapInstruction<>(isBlocking, handler, lastStep);
        onInstructionAdded(instruction);
        instructionChain.add(instruction);
    }

    @FunctionalInterface
    protected interface AsyncMapHandler {
        LuxisAsync<?> handle(Object state, Object transport, Object appState, PendingAsyncResponses pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    protected <OUT> void addAsyncMapInstruction(final boolean isBlocking, final AsyncMapHandler handler, final AsyncMapConfig config) {
        final LuxisMapInstruction.AsyncHandler<ERR> wrapped = (state, transport, app, par) -> {
            final Function<HttpErrorResponse, ERR> errMapper = asyncErrorMapper();
            final Supplier<CompletableFuture<Result<ERR, OUT>>> attemptSupplier =
                    () -> ((LuxisAsync<OUT>) handler.handle(state, transport, app, par))
                            .toCompletableFuture()
                            .thenApply(r -> r.mapError(errMapper));

            final CompletableFuture<Result<ERR, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(resultFuture, attemptSupplier,
                        config, pendingAsyncResponses, config.maxRetries);
            } else {
                pendingAsyncResponses.scheduleTimeout(config.timeoutMillis, () -> {
                    resultFuture.completeExceptionally(
                            new RuntimeException("Correlated async response timed out"));
                }, ScheduleType.TIMEOUT);
                attemptSupplier.get().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result);
                    }
                });
            }
            return (CompletableFuture<Result<ERR, ?>>) (CompletableFuture<?>) handleAsyncException(resultFuture);
        };
        addAsyncInstruction(isBlocking, wrapped, false);
    }
}
