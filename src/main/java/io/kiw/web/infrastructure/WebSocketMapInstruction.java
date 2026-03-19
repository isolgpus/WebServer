package io.kiw.web.infrastructure;

import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;

public class WebSocketMapInstruction<IN, OUT, APP> {
    final boolean isBlocking;
    public final boolean isAsync;
    private final WebSocketStreamFlatMapper<IN, OUT, APP> consumer;
    private final WebSocketStreamBlockingFlatMapper<IN, OUT> blockingConsumer;
    private final WebSocketStreamAsyncFlatMapper<IN, OUT, APP> asyncConsumer;
    private final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer;
    final boolean lastStep;

    WebSocketMapInstruction(boolean isBlocking, WebSocketStreamFlatMapper<IN, OUT, APP> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.consumer = consumer;
        this.blockingConsumer = null;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    WebSocketMapInstruction(boolean isBlocking, WebSocketStreamBlockingFlatMapper<IN, OUT> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.consumer = null;
        this.blockingConsumer = consumer;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    WebSocketMapInstruction(WebSocketStreamAsyncFlatMapper<IN, OUT, APP> asyncConsumer, boolean lastStep) {
        this.isBlocking = false;
        this.isAsync = true;
        this.consumer = null;
        this.blockingConsumer = null;
        this.asyncConsumer = asyncConsumer;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    WebSocketMapInstruction(WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer, boolean lastStep) {
        this.isBlocking = true;
        this.isAsync = true;
        this.consumer = null;
        this.blockingConsumer = null;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = asyncBlockingConsumer;
        this.lastStep = lastStep;
    }

    public Result<ErrorMessageResponse, OUT> handle(IN state, WebSocketConnection connection, APP applicationState) {
        if (consumer != null) {
            return consumer.handle(new WebSocketContext<>(state, connection, applicationState));
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(new WebSocketBlockingContext<>(state, connection));
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }

    public CompletableFuture<Result<ErrorMessageResponse, OUT>> handleAsync(IN state, WebSocketConnection connection, APP applicationState) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new WebSocketContext<>(state, connection, applicationState));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new WebSocketBlockingContext<>(state, connection));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
