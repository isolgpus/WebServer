package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.AsyncRouteContext;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RestrictedBlockingAsyncRouteContext;
import io.kiw.luxis.web.internal.RestrictedBlockingRouteContext;
import io.kiw.luxis.web.internal.RouteContext;
import io.kiw.luxis.web.internal.ScheduleType;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.validation.Validator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class WebSocketStream<IN, APP, RESP, ERR, SESSION> {
    private final List<MapInstruction> instructionChain;
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;
    private final ErrorMessageResponseMapper<ERR> errorMessageResponseMapper;

    public WebSocketStream(final List<MapInstruction> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final ErrorMessageResponseMapper<ERR> errorMessageResponseMapper) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.errorMessageResponseMapper = errorMessageResponseMapper;
    }

    public WebSocketStream<IN, APP, RESP, ERR, SESSION> validate(final Consumer<Validator<IN>> config) {
        final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, IN> mapper = ctx -> {
            final Validator<IN> v = new Validator<>(ctx.in(), "");
            config.accept(v);
            return v.toResult().mapError(errorMessageResponseMapper::map);
        };
        final MapInstruction<IN, IN, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, false);
        e.markAsValidation();
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  map(final StreamMapper<RouteContext<IN, APP, SESSION>, OUT> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  flatMap(final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }

        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper);
    }

    public WebSocketStream<IN, APP, RESP, ERR, SESSION> peek(final StreamPeeker<RouteContext<IN, APP, SESSION>> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public WebSocketStream<IN, APP, RESP, ERR, SESSION> blockingPeek(final StreamPeeker<RestrictedBlockingRouteContext<IN>> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  blockingMap(final StreamMapper<RestrictedBlockingRouteContext<IN>, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  blockingFlatMap(final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, Object, SESSION, ERR> e =
                MapInstruction.blocking(mapper, (in, session) -> new RestrictedBlockingRouteContext<>(in), false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  asyncMap(final StreamAsyncMapper<AsyncRouteContext<IN, APP, SESSION, ERR>, OUT, ERR> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  asyncMap(final StreamAsyncMapper<AsyncRouteContext<IN, APP, SESSION, ERR>, OUT, ERR> handler, final AsyncMapConfig config) {
        final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, SESSION, ERR>, ERR, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<ERR, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT, ERR> luxisAsync = handler.handle(ctx);
                            return luxisAsync.toCompletableFuture();
                        },
                        config,
                        pendingAsyncResponses,
                        config.maxRetries
                );
            } else {
                pendingAsyncResponses.scheduleTimeout(config.timeoutMillis, () -> {
                    resultFuture.completeExceptionally(new RuntimeException("Correlated async response timed out"));
                }, ScheduleType.TIMEOUT);
                final LuxisAsync<OUT, ERR> luxisAsync = handler.handle(ctx);
                luxisAsync.toCompletableFuture().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result);
                    }
                });
            }
            return resultFuture.exceptionally(throwable -> {
                final Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                pendingAsyncResponses.reportException(
                        cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                return Result.error(errorMessageResponseMapper.map(new ErrorMessageResponse("Something went wrong")));
            });
        };
        final MapInstruction<IN, OUT, APP, SESSION, ERR> e = MapInstruction.nonBlockingAsync(wrapper, false, httpErr -> errorMessageResponseMapper.map(httpErr.errorMessageValue()));
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  asyncBlockingMap(final StreamAsyncMapper<RestrictedBlockingAsyncRouteContext<IN, ERR>, OUT, ERR> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> WebSocketStream<OUT, APP, RESP, ERR, SESSION>  asyncBlockingMap(final StreamAsyncMapper<RestrictedBlockingAsyncRouteContext<IN, ERR>, OUT, ERR> handler, final AsyncMapConfig config) {
        final StreamAsyncFlatMapper<RestrictedBlockingAsyncRouteContext<IN, ERR>, ERR, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<ERR, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT, ERR> luxisAsync = handler.handle(ctx);
                            return luxisAsync.toCompletableFuture()
                                    .thenApply(result -> result);
                        },
                        config,
                        pendingAsyncResponses,
                        config.maxRetries
                );
            } else {
                pendingAsyncResponses.scheduleTimeout(config.timeoutMillis, () -> {
                    resultFuture.completeExceptionally(new RuntimeException("Correlated async response timed out"));
                }, ScheduleType.TIMEOUT);
                final LuxisAsync<OUT, ERR> luxisAsync = handler.handle(ctx);
                luxisAsync.toCompletableFuture().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result);
                    }
                });
            }
            return resultFuture.exceptionally(throwable -> {
                final Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                pendingAsyncResponses.reportException(
                        cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
                return Result.error(errorMessageResponseMapper.map(new ErrorMessageResponse("Something went wrong")));
            });
        };
        final MapInstruction<IN, OUT, Object, SESSION, ERR> e =
                MapInstruction.blockingAsync(wrapper, (in, session, par) -> new RestrictedBlockingAsyncRouteContext<>(in, par, httpErr -> errorMessageResponseMapper.map(httpErr.errorMessageValue())), false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper);
    }

    public <OUT> WebSocketPipeline<OUT> complete(final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<IN> complete() {
        final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, IN> mapper =
                ctx -> Result.success(ctx.in());
        final MapInstruction<IN, IN, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<Void> completeWithNoResponse() {
        return new WebSocketPipeline<>(instructionChain, applicationState, false);
    }

    public <OUT> WebSocketPipeline<OUT> blockingComplete(final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, Object, SESSION, ERR> e =
                MapInstruction.blocking(mapper, (in, session) -> new RestrictedBlockingRouteContext<>(in), true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }
}
