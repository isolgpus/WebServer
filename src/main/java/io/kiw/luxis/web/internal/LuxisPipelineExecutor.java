package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.TransactionManager;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.StreamPeeker;
import io.kiw.luxis.web.pipeline.TransactionRouteContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class LuxisPipelineExecutor<SESSION> {

    enum ThreadContext { APPLICATION_CONTEXT, BLOCKING }

    private final Object appState;
    private final Consumer<Exception> exceptionHandler;
    private final ExecutionDispatcher executionDispatcher;
    private final PendingAsyncResponses pendingAsyncResponses;
    private final LuxisPipelineHandler<SESSION> handler;
    private final TransactionManager<?> transactionManager;

    public LuxisPipelineExecutor(final Object appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final PendingAsyncResponses pendingAsyncResponses, final LuxisPipelineHandler<SESSION> handler) {
        this(appState, exceptionHandler, executionDispatcher, pendingAsyncResponses, handler, null);
    }

    public LuxisPipelineExecutor(final Object appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final PendingAsyncResponses pendingAsyncResponses, final LuxisPipelineHandler<SESSION> handler, final TransactionManager<?> transactionManager) {
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.executionDispatcher = executionDispatcher;
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.handler = handler;
        this.transactionManager = transactionManager;
    }

    @SuppressWarnings("unchecked")
    public void execute(final SESSION session, final LuxisPipeline<?> pipeline, final Object message) {
        final MapInstruction firstInstruction = pipeline.getApplicationInstructions().getFirst();
        executeInstruction(session, pipeline, firstInstruction, message, ThreadContext.APPLICATION_CONTEXT);
    }

    private <IN, OUT, APP> void executeInstruction(final SESSION session, final LuxisPipeline<?> pipeline, final MapInstruction<IN, OUT, APP, SESSION, ErrorMessageResponse> instruction, final IN message, final ThreadContext currentThread) {
        final ThreadContext requiredThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;

        runOnThread(requiredThread, currentThread, () -> {
            handleAndContinue(session, pipeline, instruction, message);
        });
    }

    @SuppressWarnings("unchecked")
    private <IN, OUT, APP> void handleAndContinue(final SESSION session, final LuxisPipeline<?> pipeline, final MapInstruction<IN, OUT, APP, SESSION, ErrorMessageResponse> instruction, final IN message) {
        if (instruction.isTransactional) {
            handleTransaction(session, pipeline, instruction, message);
            return;
        }
        if (instruction.isAsync) {
            final CompletableFuture<Result<ErrorMessageResponse, OUT>> future;
            try {
                future = instruction.handleAsync(message, session, (APP) appState, pendingAsyncResponses);
            } catch (final Exception e) {
                exceptionHandler.accept(e);
                return;
            }

            executionDispatcher.handleOnApplicationContext(future, exceptionHandler, (r) -> {
                try {
                    r.consume(e -> {
                        handler.handleFailure(session, instruction, e);
                    }, q -> {
                        continueChain(session, pipeline, instruction, q, ThreadContext.APPLICATION_CONTEXT);
                    });
                } catch (final Exception e) {
                    exceptionHandler.accept(e);
                }
            });

        } else {
            final ThreadContext afterThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;
            final Result<ErrorMessageResponse, OUT> result;
            try {
                result = instruction.handle(message, session, (APP) appState);
            } catch (final Exception e) {
                exceptionHandler.accept(e);
                return;
            }
            result.consume(e -> {
                runOnThread(ThreadContext.APPLICATION_CONTEXT, afterThread, () -> {
                    handler.handleFailure(session, instruction, e);
                });
            }, q -> {
                continueChain(session, pipeline, instruction, q, afterThread);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void continueChain(final SESSION session, final LuxisPipeline<?> pipeline, final MapInstruction<?, ?, ?, ?, ?> instruction, final Object result, final ThreadContext currentThread) {
        if (instruction.next().isPresent()) {
            final MapInstruction next = instruction.next().get();
            executeInstruction(session, pipeline, next, result, currentThread);
        } else if (pipeline.shouldSendResponse()) {
            runOnThread(ThreadContext.APPLICATION_CONTEXT, currentThread, () -> {
                handler.sendFinalResponse(session, result);
            });
        }
    }

    private void runOnThread(final ThreadContext required, final ThreadContext current, final Runnable action) {
        if (current == required) {
            action.run();
        } else if (required == ThreadContext.BLOCKING) {
            executionDispatcher.handleBlocking(action);
        } else {
            executionDispatcher.handleOnApplicationContext(action);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <IN, OUT, APP> void handleTransaction(
            final SESSION session,
            final LuxisPipeline<?> pipeline,
            final MapInstruction<IN, OUT, APP, SESSION, ErrorMessageResponse> instruction,
            final IN message) {
        if (transactionManager == null) {
            exceptionHandler.accept(new IllegalStateException(
                    "Encountered transactional instruction but no TransactionManager is registered."));
            return;
        }
        final TransactionManager tm = (TransactionManager) transactionManager;
        final TransactionSubChain<APP, ErrorMessageResponse, SESSION> subChain =
                (TransactionSubChain<APP, ErrorMessageResponse, SESSION>) instruction.transactionSubChain();

        final CompletableFuture<Object> beginCf;
        try {
            beginCf = tm.begin().toCompletionStage().toCompletableFuture();
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            return;
        }

        executionDispatcher.handleOnApplicationContext(beginCf, exceptionHandler, tx -> {
            runSubChainStep(session, pipeline, instruction, subChain, 0, (Object) message, tx, tm);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <IN, OUT, APP> void runSubChainStep(
            final SESSION session,
            final LuxisPipeline<?> pipeline,
            final MapInstruction<IN, OUT, APP, SESSION, ErrorMessageResponse> instruction,
            final TransactionSubChain<APP, ErrorMessageResponse, SESSION> subChain,
            final int idx,
            final Object currentValue,
            final Object tx,
            final TransactionManager tm) {
        final List<TransactionStep<?, ?, APP, ErrorMessageResponse, SESSION>> steps = subChain.steps();
        if (idx >= steps.size()) {
            commitAndFinalize(session, pipeline, instruction, subChain, currentValue, tx, tm);
            return;
        }

        final TransactionStep step = steps.get(idx);
        final TransactionRouteContext ctx = new TransactionRouteContext<>(currentValue, appState, session);

        if (step.kind() == TransactionStep.Kind.SYNC) {
            final Result<ErrorMessageResponse, Object> result;
            TransactionStatus.enter();
            try {
                result = step.syncMapper().handle(ctx);
            } catch (final Exception e) {
                rollback(tm, tx, () -> exceptionHandler.accept(e));
                return;
            } finally {
                TransactionStatus.exit();
            }
            result.consume(err -> {
                rollback(tm, tx, () -> handler.handleFailure(session, instruction, err));
            }, ok -> {
                runSubChainStep(session, pipeline, instruction, subChain, idx + 1, ok, tx, tm);
            });
        } else {
            final io.kiw.luxis.web.http.client.LuxisAsync<Object, ErrorMessageResponse> luxisAsync;
            TransactionStatus.enter();
            try {
                luxisAsync = step.asyncMapper().handle(ctx);
            } catch (final Exception e) {
                rollback(tm, tx, () -> exceptionHandler.accept(e));
                return;
            } finally {
                TransactionStatus.exit();
            }
            final CompletableFuture<Result<ErrorMessageResponse, Object>> cf = luxisAsync.toCompletableFuture();
            cf.whenComplete((result, err) -> {
                if (err != null) {
                    final Throwable cause = err instanceof CompletionException ? err.getCause() : err;
                    final Exception ex = cause instanceof Exception ? (Exception) cause : new RuntimeException(cause);
                    executionDispatcher.handleOnApplicationContext(() ->
                            rollback(tm, tx, () -> exceptionHandler.accept(ex)));
                } else {
                    executionDispatcher.handleOnApplicationContext(() ->
                            result.consume(
                                    errValue -> rollback(tm, tx, () -> handler.handleFailure(session, instruction, errValue)),
                                    ok -> runSubChainStep(session, pipeline, instruction, subChain, idx + 1, ok, tx, tm)));
                }
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void rollback(final TransactionManager tm, final Object tx, final Runnable afterRollback) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <IN, OUT, APP> void commitAndFinalize(
            final SESSION session,
            final LuxisPipeline<?> pipeline,
            final MapInstruction<IN, OUT, APP, SESSION, ErrorMessageResponse> instruction,
            final TransactionSubChain<APP, ErrorMessageResponse, SESSION> subChain,
            final Object finalValue,
            final Object tx,
            final TransactionManager tm) {
        final CompletableFuture<Void> commitCf;
        try {
            commitCf = tm.commit(tx).toCompletionStage().toCompletableFuture();
        } catch (final Exception e) {
            exceptionHandler.accept(e);
            return;
        }
        executionDispatcher.handleOnApplicationContext(commitCf, exceptionHandler, v -> {
            fireOnCompletionHooks(subChain, finalValue, tx, tm, session);
            continueChain(session, pipeline, instruction, finalValue, ThreadContext.APPLICATION_CONTEXT);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <APP> void fireOnCompletionHooks(
            final TransactionSubChain<APP, ErrorMessageResponse, SESSION> subChain,
            final Object finalValue,
            final Object tx,
            final TransactionManager tm,
            final SESSION session) {
        final List<StreamPeeker<TransactionRouteContext<?, APP, SESSION>>> hooks = subChain.onCompletionHooks();
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
