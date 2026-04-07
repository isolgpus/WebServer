package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LuxisMapInstruction<ERR> {
    public final boolean isBlocking;
    public final boolean isAsync;
    public final boolean lastStep;

    @FunctionalInterface
    public interface SyncHandler<ERR> {
        Result<ERR, ?> handle(Object state, Object transport, Object appState);
    }

    @FunctionalInterface
    public interface AsyncHandler<ERR> {
        CompletableFuture<Result<ERR, ?>> handle(Object state, Object transport, Object appState, PendingAsyncResponses pendingAsyncResponses);
    }

    private final SyncHandler<ERR> syncHandler;
    private final AsyncHandler<ERR> asyncHandler;

    private boolean isValidation;
    private Optional<LuxisMapInstruction<ERR>> next = Optional.empty();

    public LuxisMapInstruction(final boolean isBlocking, final SyncHandler<ERR> syncHandler, final boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.syncHandler = syncHandler;
        this.asyncHandler = null;
        this.lastStep = lastStep;
    }

    public LuxisMapInstruction(final boolean isBlocking, final AsyncHandler<ERR> asyncHandler, final boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = true;
        this.syncHandler = null;
        this.asyncHandler = asyncHandler;
        this.lastStep = lastStep;
    }

    @SuppressWarnings("unchecked")
    public <IN, OUT> Result<ERR, OUT> handle(final Object state, final Object transport, final Object applicationState) {
        if (syncHandler != null) {
            return (Result<ERR, OUT>) syncHandler.handle(state, transport, applicationState);
        }
        throw new UnsupportedOperationException("Unknown consumer");
    }

    @SuppressWarnings("unchecked")
    public <IN, OUT> CompletableFuture<Result<ERR, OUT>> handleAsync(final Object state, final Object transport, final Object applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        if (asyncHandler != null) {
            return (CompletableFuture<Result<ERR, OUT>>) (CompletableFuture<?>) asyncHandler.handle(state, transport, applicationState, pendingAsyncResponses);
        }
        throw new UnsupportedOperationException("Unknown async consumer");
    }

    public void markAsValidation() {
        this.isValidation = true;
    }

    public boolean isValidation() {
        return isValidation;
    }

    public void setNext(final LuxisMapInstruction<ERR> next) {
        this.next = Optional.of(next);
    }

    public Optional<LuxisMapInstruction<ERR>> next() {
        return next;
    }
}
