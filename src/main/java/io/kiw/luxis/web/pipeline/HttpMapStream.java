package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.BlockingAsyncRouteContext;
import io.kiw.luxis.web.http.BlockingRouteContext;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.AsyncHttpRouteContext;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.internal.HttpRouteContext;
import io.kiw.luxis.web.internal.ScheduleType;
import io.kiw.luxis.web.internal.ender.Ender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpMapStream<IN, APP> {
    protected final List<MapInstruction> instructionChain;
    protected final boolean canFinishSuccessfully;
    protected final APP applicationState;
    protected final Ender ender;
    protected final PendingAsyncResponses pendingAsyncResponses;

    public HttpMapStream(final List<MapInstruction> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender, final PendingAsyncResponses pendingAsyncResponses) {
        this.instructionChain = instructionChain;
        this.canFinishSuccessfully = canFinishSuccessfully;
        this.applicationState = applicationState;
        this.ender = ender;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public <OUT> HttpMapStream<OUT, APP> map(final StreamMapper<HttpRouteContext<IN, APP>, OUT> flowHandler) {

        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> flatMap(final StreamFlatMapper<HttpRouteContext<IN, APP>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.nonBlocking(mapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }


    public HttpMapStream<IN, APP> peek(final StreamPeeker<HttpRouteContext<IN, APP>> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public HttpMapStream<IN, APP> blockingPeek(final StreamPeeker<BlockingRouteContext<IN>> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public <OUT> HttpMapStream<OUT, APP> blockingMap(final StreamMapper<BlockingRouteContext<IN>, OUT> flowHandler) {

        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> blockingFlatMap(final StreamFlatMapper<BlockingRouteContext<IN>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.blocking(mapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final StreamAsyncMapper<AsyncHttpRouteContext<IN, APP>, OUT> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final StreamAsyncMapper<AsyncHttpRouteContext<IN, APP>, OUT> handler, final AsyncMapConfig config) {
        final StreamAsyncFlatMapper<AsyncHttpRouteContext<IN, APP>, HttpErrorResponse, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<HttpErrorResponse, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
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
                final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
                luxisAsync.toCompletableFuture().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result);
                    }
                });
            }
            return resultFuture;
        };
        instructionChain.add(MapInstruction.nonBlockingAsync(wrapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final StreamAsyncMapper<BlockingAsyncRouteContext<IN>, OUT> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final StreamAsyncMapper<BlockingAsyncRouteContext<IN>, OUT> handler, final AsyncMapConfig config) {
        final StreamAsyncFlatMapper<BlockingAsyncRouteContext<IN>, HttpErrorResponse, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<HttpErrorResponse, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
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
                final LuxisAsync<OUT> luxisAsync = handler.handle(ctx);
                luxisAsync.toCompletableFuture().whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        resultFuture.completeExceptionally(throwable);
                    } else {
                        resultFuture.complete(result);
                    }
                });
            }
            return resultFuture;
        };
        instructionChain.add(MapInstruction.blockingAsync(wrapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> RequestPipeline<OUT> complete(final StreamFlatMapper<HttpRouteContext<IN, APP>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.nonBlocking(mapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public RequestPipeline<Void> complete() {
        return complete(a -> HttpResult.success());
    }


    public <OUT> RequestPipeline<OUT> blockingComplete(final StreamFlatMapper<BlockingRouteContext<IN>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.blocking(mapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
