package io.kiw.template.web.infrastructure;

import io.kiw.result.Result;

public class MapInstruction<IN, OUT, APP> {
    final boolean isBlocking;
    private final HttpControlStreamFlatMapper<IN, OUT, APP> consumer;
    private final HttpControlStreamBlockingFlatMapper<IN, OUT> blockingConsumer;
    final boolean lastStep;

    MapInstruction(boolean isBlocking, HttpControlStreamFlatMapper<IN, OUT, APP> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.consumer = consumer;
        this.blockingConsumer = null;
        this.lastStep = lastStep;
    }

    MapInstruction(boolean isBlocking, HttpControlStreamBlockingFlatMapper<IN, OUT> consumer, boolean lastStep) {
        this.isBlocking = isBlocking;
        this.consumer = null;
        this.blockingConsumer = consumer;
        this.lastStep = lastStep;
    }


    public Result<HttpErrorResponse, OUT> handle(IN state, HttpContext httpContext, APP applicationState) {
        if (consumer != null) {
            return consumer.handle(state, httpContext, applicationState);
        } else if (blockingConsumer != null) {
            return blockingConsumer.handle(state, httpContext);
        }

        throw new UnsupportedOperationException("Unknown consumer");
    }
}
