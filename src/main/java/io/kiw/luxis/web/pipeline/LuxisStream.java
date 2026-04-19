package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.AsyncRouteContext;
import io.kiw.luxis.web.internal.LuxisPipeline;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RestrictedBlockingAsyncRouteContext;
import io.kiw.luxis.web.internal.RestrictedBlockingRouteContext;
import io.kiw.luxis.web.internal.RouteContext;
import io.kiw.luxis.web.internal.ScheduleType;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.validation.Validator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class LuxisStream<IN, APP, RESP, ERR, SESSION> {
    protected final List<MapInstruction> instructionChain;
    protected final APP applicationState;
    protected final PendingAsyncResponses pendingAsyncResponses;
    protected final ErrorMessageResponseMapper<ERR> errorMessageResponseMapper;
    protected final Ender ender;

    public LuxisStream(final List<MapInstruction> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final ErrorMessageResponseMapper<ERR> errorMessageResponseMapper) {
        this(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper, null);
    }

    public LuxisStream(final List<MapInstruction> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses, final ErrorMessageResponseMapper<ERR> errorMessageResponseMapper, final Ender ender) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
        this.errorMessageResponseMapper = errorMessageResponseMapper;
        this.ender = ender;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void appendInstruction(final MapInstruction<?, ?, ?, ?, ?> e) {
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
    }

    protected void appendValidation(final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, IN> mapper) {
        final MapInstruction<IN, IN, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, false);
        e.markAsValidation();
        appendInstruction(e);
    }

    public LuxisStream<IN, APP, RESP, ERR, SESSION> validate(final Consumer<Validator<IN, ERR>> config) {
        appendValidation(ctx -> {
            final Validator<IN, ERR> v = new Validator<>(ctx.in(), "", errorMessageResponseMapper);
            config.accept(v);
            return v.toResult();
        });
        return new LuxisStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper, ender);
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> map(final StreamMapper<RouteContext<IN, APP, SESSION>, OUT> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> flatMap(final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, false);
        appendInstruction(e);
        return new LuxisStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper, ender);
    }

    public LuxisStream<IN, APP, RESP, ERR, SESSION> peek(final StreamPeeker<RouteContext<IN, APP, SESSION>> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public LuxisStream<IN, APP, RESP, ERR, SESSION> blockingPeek(final StreamPeeker<RestrictedBlockingRouteContext<IN>> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> blockingMap(final StreamMapper<RestrictedBlockingRouteContext<IN>, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> blockingFlatMap(final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, Object, SESSION, ERR> e =
                MapInstruction.blocking(mapper, (in, session) -> new RestrictedBlockingRouteContext<>(in), false);
        appendInstruction(e);
        return new LuxisStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper, ender);
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> asyncMap(final StreamAsyncMapper<AsyncRouteContext<IN, APP, SESSION, ERR>, OUT, ERR> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> asyncMap(final StreamAsyncMapper<AsyncRouteContext<IN, APP, SESSION, ERR>, OUT, ERR> handler, final AsyncMapConfig config) {
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
                return Result.error(errorMessageResponseMapper.map(new ErrorMessageResponse("Something went wrong"), ErrorCause.ASYNC_ERROR));
            });
        };
        final MapInstruction<IN, OUT, APP, SESSION, ERR> e = MapInstruction.nonBlockingAsync(wrapper, false, httpErr -> errorMessageResponseMapper.map(httpErr.errorMessageValue(), ErrorCause.HTTP_CLIENT_ERROR));
        appendInstruction(e);
        return new LuxisStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper, ender);
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> asyncBlockingMap(final StreamAsyncMapper<RestrictedBlockingAsyncRouteContext<IN, ERR>, OUT, ERR> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> LuxisStream<OUT, APP, RESP, ERR, SESSION> asyncBlockingMap(final StreamAsyncMapper<RestrictedBlockingAsyncRouteContext<IN, ERR>, OUT, ERR> handler, final AsyncMapConfig config) {
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
                return Result.error(errorMessageResponseMapper.map(new ErrorMessageResponse("Something went wrong"), ErrorCause.ASYNC_ERROR));
            });
        };
        final MapInstruction<IN, OUT, Object, SESSION, ERR> e =
                MapInstruction.blockingAsync(wrapper, (in, session, par) -> new RestrictedBlockingAsyncRouteContext<>(in, par, httpErr -> errorMessageResponseMapper.map(httpErr.errorMessageValue(), ErrorCause.HTTP_CLIENT_ERROR)), false);
        appendInstruction(e);
        return new LuxisStream<>(instructionChain, applicationState, pendingAsyncResponses, errorMessageResponseMapper, ender);
    }

    public <OUT> LuxisPipeline<OUT> complete(final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, ender != null);
        appendInstruction(e);
        return new LuxisPipeline<>(instructionChain, applicationState, true, ender);
    }

    public LuxisPipeline<IN> complete() {
        final StreamFlatMapper<RouteContext<IN, APP, SESSION>, ERR, IN> mapper =
                ctx -> Result.success(ctx.in());
        final MapInstruction<IN, IN, APP, SESSION, ERR> e = MapInstruction.nonBlocking(mapper, ender != null);
        appendInstruction(e);
        return new LuxisPipeline<>(instructionChain, applicationState, true, ender);
    }

    public LuxisPipeline<Void> completeWithNoResponse() {
        return new LuxisPipeline<>(instructionChain, applicationState, false, ender);
    }

    public <OUT> LuxisPipeline<OUT> blockingComplete(final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ERR, OUT> mapper) {
        final MapInstruction<IN, OUT, Object, SESSION, ERR> e =
                MapInstruction.blocking(mapper, (in, session) -> new RestrictedBlockingRouteContext<>(in), ender != null);
        appendInstruction(e);
        return new LuxisPipeline<>(instructionChain, applicationState, true, ender);
    }
}
