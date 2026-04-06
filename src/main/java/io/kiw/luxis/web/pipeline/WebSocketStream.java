package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.ScheduleType;
import io.kiw.luxis.web.internal.WebSocketMapInstruction;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.validation.WebSocketValidator;
import io.kiw.luxis.web.websocket.WebSocketResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class WebSocketStream<IN, APP, RESP> {
    private final List<WebSocketMapInstruction> instructionChain;
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketStream(final List<WebSocketMapInstruction> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public WebSocketStream<IN, APP, RESP> validate(final Consumer<WebSocketValidator<IN>> config) {
        final WebSocketMapInstruction<IN, IN, APP, RESP> e = new WebSocketMapInstruction<>(false,
                (WebSocketStreamFlatMapper<IN, IN, APP, RESP>) ctx -> {
                    final WebSocketValidator<IN> v = new WebSocketValidator<>(ctx.in(), "");
                    config.accept(v);
                    return v.toResult();
                }, false);
        e.markAsValidation();
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> map(final WebSocketStreamMapper<IN, OUT, APP, RESP> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> flatMap(final WebSocketStreamFlatMapper<IN, OUT, APP, RESP> mapper) {
        final WebSocketMapInstruction<IN, OUT, APP, RESP> e = new WebSocketMapInstruction<>(false, mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }

        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public WebSocketStream<IN, APP, RESP> peek(final WebSocketStreamPeeker<IN, APP, RESP> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public WebSocketStream<IN, APP, RESP> blockingPeek(final WebSocketStreamBlockingPeeker<IN> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> blockingMap(final WebSocketStreamBlockingMapper<IN, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> blockingFlatMap(final WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        final WebSocketMapInstruction<IN, OUT, Object, RESP> e = new WebSocketMapInstruction<>(true, mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> asyncMap(final WebSocketStreamAsyncMapper<IN, OUT, APP, RESP> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> asyncMap(final WebSocketStreamAsyncMapper<IN, OUT, APP, RESP> handler, final AsyncMapConfig config) {
        final WebSocketStreamAsyncFlatMapper<IN, OUT, APP, RESP> wrapper = ctx -> {
            final CompletableFuture<Result<ErrorMessageResponse, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
                            return luxisAsync.toCompletableFuture()
                                    .thenApply(result -> result.mapError(HttpErrorResponse::errorMessageValue));
                        },
                        config,
                        pendingAsyncResponses,
                        config.maxRetries
                );
            } else {
                pendingAsyncResponses.scheduleTimeout(config.timeoutMillis, () -> {
                    resultFuture.completeExceptionally(new RuntimeException("Correlated async response timed out"));
                }, ScheduleType.TIMEOUT);
                final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
                luxisAsync.toCompletableFuture().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result.mapError(HttpErrorResponse::errorMessageValue));
                    }
                });
            }
            return resultFuture.exceptionally(throwable -> {
                final Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                pendingAsyncResponses.reportException(
                        cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                return Result.error(new ErrorMessageResponse("Something went wrong"));
            });
        };
        final WebSocketMapInstruction<IN, OUT, APP, RESP> e = new WebSocketMapInstruction<>(wrapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> asyncBlockingMap(final WebSocketStreamAsyncBlockingMapper<IN, OUT> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> asyncBlockingMap(final WebSocketStreamAsyncBlockingMapper<IN, OUT> handler, final AsyncMapConfig config) {
        final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<ErrorMessageResponse, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
                            return luxisAsync.toCompletableFuture()
                                    .thenApply(result -> result.mapError(HttpErrorResponse::errorMessageValue));
                        },
                        config,
                        pendingAsyncResponses,
                        config.maxRetries
                );
            } else {
                pendingAsyncResponses.scheduleTimeout(config.timeoutMillis, () -> {
                    resultFuture.completeExceptionally(new RuntimeException("Correlated async response timed out"));
                }, ScheduleType.TIMEOUT);
                final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
                luxisAsync.toCompletableFuture().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result.mapError(HttpErrorResponse::errorMessageValue));
                    }
                });
            }
            return resultFuture.exceptionally(throwable -> {
                final Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                pendingAsyncResponses.reportException(
                        cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                return Result.error(new ErrorMessageResponse("Something went wrong"));
            });
        };
        final WebSocketMapInstruction<IN, OUT, Object, RESP> e = new WebSocketMapInstruction<>(wrapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketPipeline<OUT> complete(final WebSocketStreamFlatMapper<IN, OUT, APP, RESP> mapper) {
        final WebSocketMapInstruction<IN, OUT, APP, RESP> e = new WebSocketMapInstruction<>(false, mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<IN> complete() {
        final WebSocketMapInstruction<IN, IN, APP, RESP> e = new WebSocketMapInstruction<>(false,
                (WebSocketStreamFlatMapper<IN, IN, APP, RESP>) ctx -> WebSocketResult.success(ctx.in()), true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<Void> completeWithNoResponse() {
        return new WebSocketPipeline<>(instructionChain, applicationState, false);
    }

    public <OUT> WebSocketPipeline<OUT> blockingComplete(final WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        final WebSocketMapInstruction<IN, OUT, Object, RESP> e = new WebSocketMapInstruction<>(true, mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }
}
