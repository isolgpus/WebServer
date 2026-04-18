package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.StreamAsyncFlatMapper;
import io.kiw.luxis.web.pipeline.StreamFlatMapper;
import io.kiw.luxis.web.websocket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class WebSocketMapInstruction<IN, OUT, APP, RESP> {
    public final boolean isBlocking;
    public final boolean isAsync;
    private final StreamFlatMapper<RouteContext<IN, APP, WebSocketSession<RESP>>, ErrorMessageResponse, OUT> consumer;
    private final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ErrorMessageResponse, OUT> blockingConsumer;
    private final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, WebSocketSession<RESP>>, ErrorMessageResponse, OUT> asyncConsumer;
    private final StreamAsyncFlatMapper<RestrictedBlockingAsyncRouteContext<IN>, ErrorMessageResponse, OUT> asyncBlockingConsumer;
    public final boolean lastStep;
    private boolean isValidation;
    private Optional<WebSocketMapInstruction<?, ?, ?, ?>> next = Optional.empty();

    private WebSocketMapInstruction(
            final boolean isBlocking,
            final boolean isAsync,
            final StreamFlatMapper<RouteContext<IN, APP, WebSocketSession<RESP>>, ErrorMessageResponse, OUT> consumer,
            final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ErrorMessageResponse, OUT> blockingConsumer,
            final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, WebSocketSession<RESP>>, ErrorMessageResponse, OUT> asyncConsumer,
            final StreamAsyncFlatMapper<RestrictedBlockingAsyncRouteContext<IN>, ErrorMessageResponse, OUT> asyncBlockingConsumer,
            final boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = isAsync;
        this.consumer = consumer;
        this.blockingConsumer = blockingConsumer;
        this.asyncConsumer = asyncConsumer;
        this.asyncBlockingConsumer = asyncBlockingConsumer;
        this.lastStep = lastStep;
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> nonBlocking(
            final StreamFlatMapper<RouteContext<IN, APP, WebSocketSession<RESP>>, ErrorMessageResponse, OUT> consumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(false, false, consumer, null, null, null, lastStep);
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> blocking(
            final StreamFlatMapper<RestrictedBlockingRouteContext<IN>, ErrorMessageResponse, OUT> consumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(true, false, null, consumer, null, null, lastStep);
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> nonBlockingAsync(
            final StreamAsyncFlatMapper<AsyncRouteContext<IN, APP, WebSocketSession<RESP>>, ErrorMessageResponse, OUT> asyncConsumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(false, true, null, null, asyncConsumer, null, lastStep);
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> blockingAsync(
            final StreamAsyncFlatMapper<RestrictedBlockingAsyncRouteContext<IN>, ErrorMessageResponse, OUT> asyncBlockingConsumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(true, true, null, null, null, asyncBlockingConsumer, lastStep);
    }

    public void markAsValidation() {
        this.isValidation = true;
    }

    public boolean isValidation() {
        return isValidation;
    }

    public void setNext(final WebSocketMapInstruction<?, ?, ?, ?> next) {
        this.next = Optional.of(next);
    }

    public Optional<WebSocketMapInstruction<?, ?, ?, ?>> next() {
        return next;
    }

    public Result<ErrorMessageResponse, OUT> handle(final IN state, final WebSocketSession<RESP> connection, final APP applicationState) {
        if (consumer != null) {
            return consumer.handle(new RouteContext<>(state, connection, applicationState));
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(new RestrictedBlockingRouteContext<>(state));
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }

    public CompletableFuture<Result<ErrorMessageResponse, OUT>> handleAsync(final IN state, final WebSocketSession<RESP> connection, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new AsyncRouteContext<>(state, connection, applicationState, pendingAsyncResponses));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new RestrictedBlockingAsyncRouteContext<>(state, pendingAsyncResponses));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
