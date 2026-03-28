package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.WebSocketMapInstruction;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.validation.WebSocketValidator;
import io.kiw.luxis.web.websocket.CorrelatedWebSocketBlockingContext;
import io.kiw.luxis.web.websocket.CorrelatedWebSocketContext;
import io.kiw.luxis.web.websocket.WebSocketResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketStream<IN, APP> {
    private final List<WebSocketMapInstruction> instructionChain;
    private final APP applicationState;
    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketStream(final List<WebSocketMapInstruction> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public WebSocketStream<IN, APP> validate(final Consumer<WebSocketValidator<IN>> config) {
        final WebSocketMapInstruction<IN, IN, APP> e = new WebSocketMapInstruction<>(false,
                (WebSocketStreamFlatMapper<IN, IN, APP>) ctx -> {
                    final WebSocketValidator<IN> v = new WebSocketValidator<>(ctx.in(), "");
                    config.accept(v);
                    return v.toResult();
                }, false);
        e.markAsValidation();
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP> map(final WebSocketStreamMapper<IN, OUT, APP> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP> flatMap(final WebSocketStreamFlatMapper<IN, OUT, APP> mapper) {
        final WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(false, mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }

        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP> blockingMap(final WebSocketStreamBlockingMapper<IN, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP> blockingFlatMap(final WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        final WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(true, mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP> correlatedAsyncMap(final WebSocketStreamCorrelatedAsyncHandler<IN, APP> handler) {
        return correlatedAsyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> WebSocketStream<OUT, APP> correlatedAsyncMap(final WebSocketStreamCorrelatedAsyncHandler<IN, APP> handler, final AsyncMapConfig config) {
        final WebSocketStreamAsyncFlatMapper<IN, OUT, APP> wrapper = ctx -> {
            final CompletableFuture<Result<HttpErrorResponse, OUT>> future = new CompletableFuture<>();
            final long correlationId = pendingAsyncResponses.register(future, config.timeoutMillis);
            handler.handle(new CorrelatedWebSocketContext<>(correlationId, ctx.in(), ctx.connection(), ctx.app()));
            return future.thenApply(result -> result.mapError(HttpErrorResponse::errorMessageValue));
        };
        final WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(wrapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP> correlatedAsyncBlockingMap(final WebSocketStreamCorrelatedAsyncBlockingHandler<IN> handler) {
        return correlatedAsyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    public <OUT> WebSocketStream<OUT, APP> correlatedAsyncBlockingMap(final WebSocketStreamCorrelatedAsyncBlockingHandler<IN> handler, final AsyncMapConfig config) {
        final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> wrapper = ctx -> {
            final CompletableFuture<Result<HttpErrorResponse, OUT>> future = new CompletableFuture<>();
            final long correlationId = pendingAsyncResponses.register(future, config.timeoutMillis);
            handler.handle(new CorrelatedWebSocketBlockingContext<>(correlationId, ctx.in()));
            return future.thenApply(result -> result.mapError(HttpErrorResponse::errorMessageValue));
        };
        final WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(wrapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketPipeline<OUT> complete(final WebSocketStreamFlatMapper<IN, OUT, APP> mapper) {
        final WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(false, mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<IN> complete() {
        final WebSocketMapInstruction<IN, IN, APP> e = new WebSocketMapInstruction<>(false,
                (WebSocketStreamFlatMapper<IN, IN, APP>) ctx -> WebSocketResult.success(ctx.in()), true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<Void> completeWithNoResponse() {
        return new WebSocketPipeline<>(instructionChain, applicationState, false);
    }

    public <OUT> WebSocketPipeline<OUT> blockingComplete(final WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        final WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(true, mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }
}
