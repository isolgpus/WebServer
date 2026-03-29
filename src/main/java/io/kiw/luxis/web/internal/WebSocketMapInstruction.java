package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.WebSocketStreamAsyncBlockingFlatMapper;
import io.kiw.luxis.web.pipeline.WebSocketStreamAsyncFlatMapper;
import io.kiw.luxis.web.pipeline.WebSocketStreamBlockingFlatMapper;
import io.kiw.luxis.web.pipeline.WebSocketStreamFlatMapper;
import io.kiw.luxis.web.websocket.WebSocketBlockingContext;
import io.kiw.luxis.web.websocket.WebSocketContext;
import io.kiw.luxis.web.websocket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WebSocketMapInstruction<IN, OUT, APP, RESP> {
    public final boolean isBlocking;
    public final boolean isAsync;
    private final WebSocketStreamFlatMapper<IN, OUT, APP, RESP> consumer;
    private final WebSocketStreamBlockingFlatMapper<IN, OUT> blockingConsumer;
    private final WebSocketStreamAsyncFlatMapper<IN, OUT, APP, RESP> asyncConsumer;
    private final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer;
    public final boolean lastStep;
    private boolean isValidation;
    private Optional<WebSocketMapInstruction<?, ?, ?, ?>> next = Optional.empty();

    public WebSocketMapInstruction(final boolean isBlocking, final WebSocketStreamFlatMapper<IN, OUT, APP, RESP> consumer, final boolean lastStep) {
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

    public WebSocketMapInstruction(final WebSocketStreamAsyncFlatMapper<IN, OUT, APP, RESP> asyncConsumer, final boolean lastStep) {
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

    public CompletableFuture<Result<ErrorMessageResponse, OUT>> handleAsync(final IN state, final WebSocketSession<RESP> connection, final APP applicationState) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new WebSocketContext<>(state, connection, applicationState));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new WebSocketBlockingContext<>(state));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
