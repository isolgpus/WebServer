package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.WebSocketStreamAsyncBlockingFlatMapper;
import io.kiw.luxis.web.pipeline.WebSocketStreamAsyncFlatMapper;
import io.kiw.luxis.web.pipeline.WebSocketStreamBlockingFlatMapper;
import io.kiw.luxis.web.pipeline.WebSocketStreamFlatMapper;
import io.kiw.luxis.web.websocket.WebSocketBlockingContext;
import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.kiw.luxis.web.websocket.WebSocketContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WebSocketMapInstruction<IN, OUT, APP> {
    public final boolean isBlocking;
    public final boolean isAsync;
    private final WebSocketStreamFlatMapper<IN, OUT, APP> consumer;
    private final WebSocketStreamBlockingFlatMapper<IN, OUT> blockingConsumer;
    private final WebSocketStreamAsyncFlatMapper<IN, OUT, APP> asyncConsumer;
    private final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer;
    public final boolean lastStep;
    private Optional<WebSocketMapInstruction> next = Optional.empty();

    public WebSocketMapInstruction(final boolean isBlocking, final WebSocketStreamFlatMapper<IN, OUT, APP> consumer, final boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.consumer = consumer;
        this.blockingConsumer = null;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    public WebSocketMapInstruction(final boolean isBlocking, final WebSocketStreamBlockingFlatMapper<IN, OUT> consumer, final boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.consumer = null;
        this.blockingConsumer = consumer;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    public WebSocketMapInstruction(final WebSocketStreamAsyncFlatMapper<IN, OUT, APP> asyncConsumer, final boolean lastStep) {
        this.isBlocking = false;
        this.isAsync = true;
        this.consumer = null;
        this.blockingConsumer = null;
        this.asyncConsumer = asyncConsumer;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    public WebSocketMapInstruction(final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer, final boolean lastStep) {
        this.isBlocking = true;
        this.isAsync = true;
        this.consumer = null;
        this.blockingConsumer = null;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = asyncBlockingConsumer;
        this.lastStep = lastStep;
    }

    public void setNext(final WebSocketMapInstruction next) {
        this.next = Optional.of(next);
    }

    public Optional<WebSocketMapInstruction> next() {
        return next;
    }

    public Result<ErrorMessageResponse, OUT> handle(final IN state, final WebSocketConnection connection, final APP applicationState) {
        if (consumer != null) {
            return consumer.handle(new WebSocketContext<>(state, connection, applicationState));
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(new WebSocketBlockingContext<>(state));
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }

    public CompletableFuture<Result<ErrorMessageResponse, OUT>> handleAsync(final IN state, final WebSocketConnection connection, final APP applicationState) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new WebSocketContext<>(state, connection, applicationState));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new WebSocketBlockingContext<>(state));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
