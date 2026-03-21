package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;

public class MapInstruction<IN, OUT, APP> {
    public final boolean isBlocking;
    public final boolean isAsync;
    private final HttpControlStreamFlatMapper<IN, OUT, APP> consumer;
    private final HttpControlStreamBlockingFlatMapper<IN, OUT> blockingConsumer;
    private final HttpControlStreamAsyncFlatMapper<IN, OUT, APP> asyncConsumer;
    private final HttpControlStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer;
    public final boolean lastStep;

    public MapInstruction(boolean isBlocking, HttpControlStreamFlatMapper<IN, OUT, APP> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.consumer = consumer;
        this.blockingConsumer = null;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    public MapInstruction(boolean isBlocking, HttpControlStreamBlockingFlatMapper<IN, OUT> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.isAsync = false;
        this.consumer = null;
        this.blockingConsumer = consumer;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    public MapInstruction(HttpControlStreamAsyncFlatMapper<IN, OUT, APP> asyncConsumer, boolean lastStep) {
        this.isBlocking = false;
        this.isAsync = true;
        this.consumer = null;
        this.blockingConsumer = null;
        this.asyncConsumer = asyncConsumer;
        this.asyncBlockingConsumer = null;
        this.lastStep = lastStep;
    }

    public MapInstruction(HttpControlStreamAsyncBlockingFlatMapper<IN, OUT> asyncBlockingConsumer, boolean lastStep) {
        this.isBlocking = true;
        this.isAsync = true;
        this.consumer = null;
        this.blockingConsumer = null;
        this.asyncConsumer = null;
        this.asyncBlockingConsumer = asyncBlockingConsumer;
        this.lastStep = lastStep;
    }

    public Result<HttpErrorResponse, OUT> handle(IN state, HttpContext httpContext, APP applicationState) {
        if (consumer != null) {
            return consumer.handle(new RouteContext<>(state, httpContext, applicationState));
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(new BlockingContext<>(state, httpContext));
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }

    public CompletableFuture<Result<HttpErrorResponse, OUT>> handleAsync(IN state, HttpContext httpContext, APP applicationState) {
        if (asyncConsumer != null) {
            return asyncConsumer.handle(new RouteContext<>(state, httpContext, applicationState));
        } else if (asyncBlockingConsumer != null) {
            return asyncBlockingConsumer.handle(new BlockingContext<>(state, httpContext));
        }

        throw new UnsupportedOperationException("Unknown async consumer");
    }
}
