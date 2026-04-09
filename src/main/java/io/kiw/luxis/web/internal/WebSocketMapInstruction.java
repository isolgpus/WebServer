package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.StreamAsyncFlatMapper;
import io.kiw.luxis.web.pipeline.StreamFlatMapper;
import io.kiw.luxis.web.websocket.WebSocketAsyncContext;
import io.kiw.luxis.web.websocket.WebSocketBlockingAsyncContext;
import io.kiw.luxis.web.websocket.WebSocketBlockingContext;
import io.kiw.luxis.web.websocket.WebSocketContext;
import io.kiw.luxis.web.websocket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class WebSocketMapInstruction<IN, OUT, APP, RESP> {
    public final boolean isBlocking;
    public final boolean isAsync;
    private final StreamFlatMapper<WebSocketContext<IN, APP, RESP>, ErrorMessageResponse, OUT> consumer;
    private final StreamFlatMapper<WebSocketBlockingContext<IN>, ErrorMessageResponse, OUT> blockingConsumer;
    private final StreamAsyncFlatMapper<WebSocketAsyncContext<IN, APP, RESP>, ErrorMessageResponse, OUT> asyncConsumer;
    private final StreamAsyncFlatMapper<WebSocketBlockingAsyncContext<IN>, ErrorMessageResponse, OUT> asyncBlockingConsumer;
    public final boolean lastStep;
    private boolean isValidation;
    private Optional<WebSocketMapInstruction<?, ?, ?, ?>> next = Optional.empty();

    private WebSocketMapInstruction(
            final boolean isBlocking,
            final boolean isAsync,
            final StreamFlatMapper<WebSocketContext<IN, APP, RESP>, ErrorMessageResponse, OUT> consumer,
            final StreamFlatMapper<WebSocketBlockingContext<IN>, ErrorMessageResponse, OUT> blockingConsumer,
            final StreamAsyncFlatMapper<WebSocketAsyncContext<IN, APP, RESP>, ErrorMessageResponse, OUT> asyncConsumer,
            final StreamAsyncFlatMapper<WebSocketBlockingAsyncContext<IN>, ErrorMessageResponse, OUT> asyncBlockingConsumer,
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
            final StreamFlatMapper<WebSocketContext<IN, APP, RESP>, ErrorMessageResponse, OUT> consumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(false, false, consumer, null, null, null, lastStep);
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> blocking(
            final StreamFlatMapper<WebSocketBlockingContext<IN>, ErrorMessageResponse, OUT> consumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(true, false, null, consumer, null, null, lastStep);
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> nonBlockingAsync(
            final StreamAsyncFlatMapper<WebSocketAsyncContext<IN, APP, RESP>, ErrorMessageResponse, OUT> asyncConsumer, final boolean lastStep) {
        return new WebSocketMapInstruction<>(false, true, null, null, asyncConsumer, null, lastStep);
    }

    public static <IN, OUT, APP, RESP> WebSocketMapInstruction<IN, OUT, APP, RESP> blockingAsync(
            final StreamAsyncFlatMapper<WebSocketBlockingAsyncContext<IN>, ErrorMessageResponse, OUT> asyncBlockingConsumer, final boolean lastStep) {
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
            return consumer.handle(new WebSocketContext<>(state, connection, applicationState));
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(new WebSocketBlockingContext<>(state));
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }

    public CompletableFuture<Result<ErrorMessageResponse, OUT>> handleAsync(final IN state, final WebSocketSession<RESP> connection, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new WebSocketAsyncContext<>(state, connection, applicationState, pendingAsyncResponses));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new WebSocketBlockingAsyncContext<>(state, pendingAsyncResponses));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
