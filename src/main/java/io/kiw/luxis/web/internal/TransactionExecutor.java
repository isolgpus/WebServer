package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.TransactionManager;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.pipeline.StreamPeeker;
import io.kiw.luxis.web.pipeline.TransactionRouteContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class TransactionExecutor {

    private final TransactionManager<?> transactionManager;
    private final ExecutionDispatcher executionDispatcher;

    public TransactionExecutor(final TransactionManager<?> transactionManager, final ExecutionDispatcher executionDispatcher) {
        this.transactionManager = transactionManager;
        this.executionDispatcher = executionDispatcher;
    }

    public interface Callbacks {
        void onSuccess(Object finalValue);

        void onSubChainError(Object errValue);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <SESSION> void execute(
            final SESSION session,
            final Object appState,
            final MapInstruction<?, ?, ?, SESSION, ?> instruction,
            final Object input,
            final Consumer<Exception> exceptionHandler,
            final Callbacks callbacks) {
        if (transactionManager == null) {
            exceptionHandler.accept(new IllegalStateException(
                    "Encountered transactional instruction but no TransactionManager is registered."));
            return;
        }
        final TransactionManager tm = (TransactionManager) transactionManager;
        final TransactionSubChain subChain = (TransactionSubChain) instruction.transactionSubChain();

        final CompletableFuture<Object> beginCf;
        try {
            beginCf = tm.begin().toCompletionStage().toCompletableFuture();
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            return;
        }

        executionDispatcher.handleOnApplicationContext(beginCf, exceptionHandler, tx ->
                runStep(session, appState, subChain, 0, input, tx, tm, exceptionHandler, callbacks));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <SESSION> void runStep(
            final SESSION session,
            final Object appState,
            final TransactionSubChain subChain,
            final int idx,
            final Object current,
            final Object tx,
            final TransactionManager tm,
            final Consumer<Exception> exceptionHandler,
            final Callbacks callbacks) {
        final List<TransactionStep> steps = (List) subChain.steps();
        if (idx >= steps.size()) {
            commit(session, appState, subChain, current, tx, tm, exceptionHandler, callbacks);
            return;
        }

        final TransactionStep step = steps.get(idx);
        final TransactionRouteContext ctx = new TransactionRouteContext<>(current, appState, session);

        if (step.kind() == TransactionStep.Kind.SYNC) {
            final Result result;
            TransactionStatus.enter();
            try {
                result = step.syncMapper().handle(ctx);
            } catch (final Exception e) {
                rollback(tm, tx, exceptionHandler, () -> exceptionHandler.accept(e));
                return;
            } finally {
                TransactionStatus.exit();
            }
            result.consume(
                    err -> rollback(tm, tx, exceptionHandler, () -> callbacks.onSubChainError(err)),
                    ok -> runStep(session, appState, subChain, idx + 1, ok, tx, tm, exceptionHandler, callbacks));
        } else {
            final LuxisAsync luxisAsync;
            TransactionStatus.enter();
            try {
                luxisAsync = step.asyncMapper().handle(ctx);
            } catch (final Exception e) {
                rollback(tm, tx, exceptionHandler, () -> exceptionHandler.accept(e));
                return;
            } finally {
                TransactionStatus.exit();
            }
            final CompletableFuture<Result> cf = luxisAsync.toCompletableFuture();
            cf.whenComplete((result, err) -> {
                if (err != null) {
                    final Throwable cause = err instanceof CompletionException ? err.getCause() : err;
                    final Exception ex = cause instanceof Exception ? (Exception) cause : new RuntimeException(cause);
                    executionDispatcher.handleOnApplicationContext(() ->
                            rollback(tm, tx, exceptionHandler, () -> exceptionHandler.accept(ex)));
                } else {
                    executionDispatcher.handleOnApplicationContext(() ->
                            result.consume(
                                    errVal -> rollback(tm, tx, exceptionHandler, () -> callbacks.onSubChainError(errVal)),
                                    ok -> runStep(session, appState, subChain, idx + 1, ok, tx, tm, exceptionHandler, callbacks)));
                }
            });
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void rollback(final TransactionManager tm, final Object tx, final Consumer<Exception> exceptionHandler, final Runnable afterRollback) {
        final CompletableFuture<Void> rollbackCf;
        try {
            rollbackCf = tm.rollback(tx).toCompletionStage().toCompletableFuture();
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            afterRollback.run();
            return;
        }
        executionDispatcher.handleOnApplicationContext(rollbackCf, exceptionHandler, v -> afterRollback.run());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <SESSION> void commit(
            final SESSION session,
            final Object appState,
            final TransactionSubChain subChain,
            final Object finalValue,
            final Object tx,
            final TransactionManager tm,
            final Consumer<Exception> exceptionHandler,
            final Callbacks callbacks) {
        final CompletableFuture<Void> commitCf;
        try {
            commitCf = tm.commit(tx).toCompletionStage().toCompletableFuture();
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            return;
        }
        executionDispatcher.handleOnApplicationContext(commitCf, exceptionHandler, v -> {
            fireOnCompletionHooks(subChain, finalValue, tx, tm, session, appState, exceptionHandler);
            callbacks.onSuccess(finalValue);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <SESSION> void fireOnCompletionHooks(
            final TransactionSubChain subChain,
            final Object finalValue,
            final Object tx,
            final TransactionManager tm,
            final SESSION session,
            final Object appState,
            final Consumer<Exception> exceptionHandler) {
        final List<StreamPeeker> hooks = (List) subChain.onCompletionHooks();
        if (hooks.isEmpty()) {
            return;
        }
        final CompletableFuture<Void> onCommittedCf;
        try {
            onCommittedCf = tm.onCommitted(tx, () -> executionDispatcher.handleOnApplicationContext(() -> {
                final TransactionRouteContext ctx = new TransactionRouteContext<>(finalValue, appState, session);
                for (final StreamPeeker hook : hooks) {
                    try {
                        hook.handle(ctx);
                    } catch (final Exception e) {
                        exceptionHandler.accept(e);
                    }
                }
            })).toCompletionStage().toCompletableFuture();
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            return;
        }
        onCommittedCf.exceptionally(err -> {
            final Throwable cause = err instanceof CompletionException ? err.getCause() : err;
            exceptionHandler.accept(cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
            return null;
        });
    }
}
