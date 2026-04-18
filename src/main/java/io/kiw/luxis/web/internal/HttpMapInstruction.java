package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpSession;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.pipeline.StreamAsyncFlatMapper;
import io.kiw.luxis.web.pipeline.StreamFlatMapper;

import java.util.concurrent.CompletableFuture;

public final class HttpMapInstruction<IN, OUT, APP> {
    public final boolean isBlocking;
    public final boolean isAsync;
    private final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> consumer;
    private final StreamFlatMapper<BlockingRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> blockingConsumer;
    private final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> asyncConsumer;
    private final StreamAsyncFlatMapper<BlockingAsyncRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> asyncBlockingConsumer;
    public final boolean lastStep;

    private HttpMapInstruction(
            final boolean isBlocking,
            final boolean isAsync,
            final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> consumer,
            final StreamFlatMapper<BlockingRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> blockingConsumer,
            final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> asyncConsumer,
            final StreamAsyncFlatMapper<BlockingAsyncRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> asyncBlockingConsumer,
            final boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = isAsync;
        this.consumer = consumer;
        this.blockingConsumer = blockingConsumer;
        this.asyncConsumer = asyncConsumer;
        this.asyncBlockingConsumer = asyncBlockingConsumer;
        this.lastStep = lastStep;
    }

    public static <IN, OUT, APP> HttpMapInstruction<IN, OUT, APP> nonBlocking(
            final StreamFlatMapper<RouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> consumer, final boolean lastStep) {
        return new HttpMapInstruction<>(false, false, consumer, null, null, null, lastStep);
    }

    public static <IN, OUT, APP> HttpMapInstruction<IN, OUT, APP> blocking(
            final StreamFlatMapper<BlockingRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> consumer, final boolean lastStep) {
        return new HttpMapInstruction<>(true, false, null, consumer, null, null, lastStep);
    }

    public static <IN, OUT, APP> HttpMapInstruction<IN, OUT, APP> nonBlockingAsync(
            final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, HttpSession>, HttpErrorResponse, OUT> asyncConsumer, final boolean lastStep) {
        return new HttpMapInstruction<>(false, true, null, null, asyncConsumer, null, lastStep);
    }

    public static <IN, OUT, APP> HttpMapInstruction<IN, OUT, APP> blockingAsync(
            final StreamAsyncFlatMapper<BlockingAsyncRouteContext<IN, HttpSession>, HttpErrorResponse, OUT> asyncBlockingConsumer, final boolean lastStep) {
        return new HttpMapInstruction<>(true, true, null, null, null, asyncBlockingConsumer, lastStep);
    }

    public Result<HttpErrorResponse, OUT> handle(final IN state, final HttpSession httpSession, final APP applicationState) {
        if (consumer != null) {
            return consumer.handle(new RouteContext<>(state, httpSession, applicationState));
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(new BlockingRouteContext<>(state, httpSession));
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }

    public CompletableFuture<Result<HttpErrorResponse, OUT>> handleAsync(final IN state, final HttpSession httpSession, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new AsyncRouteContext<>(state, httpSession, applicationState, pendingAsyncResponses));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new BlockingAsyncRouteContext<>(state, httpSession, pendingAsyncResponses));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
