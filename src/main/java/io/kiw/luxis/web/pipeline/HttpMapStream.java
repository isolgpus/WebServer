package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpSession;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.internal.AsyncRouteContext;
import io.kiw.luxis.web.internal.BlockingAsyncRouteContext;
import io.kiw.luxis.web.internal.BlockingRouteContext;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.internal.RouteContext;
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

    public <OUT> HttpMapStream<OUT, APP> map(final StreamMapper<RouteContext<IN, APP, HttpSession>, OUT> flowHandler) {

        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> flatMap(final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.nonBlocking(mapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }


    public HttpMapStream<IN, APP> peek(final StreamPeeker<RouteContext<IN, APP, HttpSession>> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public HttpMapStream<IN, APP> blockingPeek(final StreamPeeker<BlockingRouteContext<IN, HttpSession>> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public <OUT> HttpMapStream<OUT, APP> blockingMap(final StreamMapper<BlockingRouteContext<IN, HttpSession>, OUT> flowHandler) {

        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> blockingFlatMap(final StreamFlatMapper<BlockingRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.blocking(mapper, (in, session) -> new BlockingRouteContext<>(in, session), false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final StreamAsyncMapper<AsyncRouteContext<IN, APP, HttpSession, HttpErrorResponse>, OUT, HttpErrorResponse> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final StreamAsyncMapper<AsyncRouteContext<IN, APP, HttpSession, HttpErrorResponse>, OUT, HttpErrorResponse> handler, final AsyncMapConfig config) {
        final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, HttpSession, HttpErrorResponse>, HttpErrorResponse, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<HttpErrorResponse, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT, HttpErrorResponse> luxisAsync = handler.handle(ctx);
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
                final LuxisAsync<OUT, HttpErrorResponse> luxisAsync = handler.handle(ctx);
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
        instructionChain.add(MapInstruction.nonBlockingAsync(wrapper, false, e -> e));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final StreamAsyncMapper<BlockingAsyncRouteContext<IN, HttpSession, HttpErrorResponse>, OUT, HttpErrorResponse> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final StreamAsyncMapper<BlockingAsyncRouteContext<IN, HttpSession, HttpErrorResponse>, OUT, HttpErrorResponse> handler, final AsyncMapConfig config) {
        final StreamAsyncFlatMapper<BlockingAsyncRouteContext<IN, HttpSession, HttpErrorResponse>, HttpErrorResponse, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<HttpErrorResponse, OUT>> resultFuture = new CompletableFuture<>();
            if (config.maxRetries > 0) {
                AsyncRetryExecutor.executeWithRetry(
                        resultFuture,
                        () -> {
                            final LuxisAsync<OUT, HttpErrorResponse> luxisAsync = handler.handle(ctx);
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
                final LuxisAsync<OUT, HttpErrorResponse> luxisAsync = handler.handle(ctx);
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
        instructionChain.add(MapInstruction.blockingAsync(wrapper, (in, session, par) -> new BlockingAsyncRouteContext<>(in, session, par, (HttpErrorResponse e) -> e), false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender, pendingAsyncResponses);
    }

    public <OUT> RequestPipeline<OUT> complete(final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.nonBlocking(mapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public RequestPipeline<Void> complete() {
        return complete(a -> HttpResult.success());
    }


    public <OUT> RequestPipeline<OUT> blockingComplete(final StreamFlatMapper<BlockingRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> mapper) {
        instructionChain.add(MapInstruction.blocking(mapper, (in, session) -> new BlockingRouteContext<>(in, session), canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
