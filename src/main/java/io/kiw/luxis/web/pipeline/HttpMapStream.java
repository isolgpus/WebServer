package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RequestPipeline;
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

    public <OUT> HttpMapStream<OUT, APP> map(final HttpControlStreamMapper<IN, OUT, APP> flowHandler) {

        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> flatMap(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }


    public HttpMapStream<IN, APP> peek(final HttpControlStreamPeeker<IN, APP> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public HttpMapStream<IN, APP> blockingPeek(final HttpControlStreamBlockingPeeker<IN> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public <OUT> HttpMapStream<OUT, APP> blockingMap(final HttpControlStreamBlockingMapper<IN, OUT> flowHandler) {

        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> blockingFlatMap(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final HttpControlStreamAsyncMapper<IN, OUT, APP> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final HttpControlStreamAsyncMapper<IN, OUT, APP> handler, final AsyncMapConfig config) {
        final HttpControlStreamAsyncFlatMapper<IN, OUT, APP> wrapper = ctx -> {
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
        instructionChain.add(new MapInstruction<>(wrapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final HttpControlStreamAsyncBlockingMapper<IN, OUT> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final HttpControlStreamAsyncBlockingMapper<IN, OUT> handler, final AsyncMapConfig config) {
        final HttpControlStreamAsyncBlockingFlatMapper<IN, OUT> wrapper = ctx -> {
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
        instructionChain.add(new MapInstruction<>(wrapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> RequestPipeline<OUT> complete(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public RequestPipeline<Void> complete() {
        return complete(a -> HttpResult.success());
    }


    public <OUT> RequestPipeline<OUT> blockingComplete(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
