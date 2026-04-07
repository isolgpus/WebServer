package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.internal.LuxisMapInstruction;
import io.kiw.luxis.web.internal.PendingAsyncResponses;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.validation.WebSocketValidator;
import io.kiw.luxis.web.websocket.WebSocketAsyncContext;
import io.kiw.luxis.web.websocket.WebSocketBlockingAsyncContext;
import io.kiw.luxis.web.websocket.WebSocketBlockingContext;
import io.kiw.luxis.web.websocket.WebSocketContext;
import io.kiw.luxis.web.websocket.WebSocketResult;
import io.kiw.luxis.web.websocket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class WebSocketStream<IN, APP, RESP> extends LuxisStream<IN, APP, ErrorMessageResponse> {

    public WebSocketStream(final List<LuxisMapInstruction<ErrorMessageResponse>> instructionChain, final APP applicationState, final PendingAsyncResponses pendingAsyncResponses) {
        super(instructionChain, applicationState, pendingAsyncResponses);
    }

    @Override
    protected void onInstructionAdded(final LuxisMapInstruction<ErrorMessageResponse> instruction) {
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(instruction);
        }
    }

    @Override
    protected Function<HttpErrorResponse, ErrorMessageResponse> asyncErrorMapper() {
        return HttpErrorResponse::errorMessageValue;
    }

    @Override
    protected <OUT> CompletableFuture<Result<ErrorMessageResponse, OUT>> handleAsyncException(
            final CompletableFuture<Result<ErrorMessageResponse, OUT>> future) {
        return future.exceptionally(throwable -> {
            final Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
            pendingAsyncResponses.reportException(
                    cause instanceof Exception ? (Exception) cause : new RuntimeException(cause));
            return Result.error(new ErrorMessageResponse("Something went wrong"));
        });
    }

    @SuppressWarnings("unchecked")
    public WebSocketStream<IN, APP, RESP> validate(final Consumer<WebSocketValidator<IN>> config) {
        final LuxisMapInstruction<ErrorMessageResponse> instruction = new LuxisMapInstruction<>(false,
                (LuxisMapInstruction.SyncHandler<ErrorMessageResponse>) (state, transport, app) -> {
                    final WebSocketValidator<IN> v = new WebSocketValidator<>((IN) state, "");
                    config.accept(v);
                    return v.toResult();
                }, false);
        instruction.markAsValidation();
        onInstructionAdded(instruction);
        instructionChain.add(instruction);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketStream<OUT, APP, RESP> map(final WebSocketStreamMapper<IN, OUT, APP, RESP> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketStream<OUT, APP, RESP> flatMap(final WebSocketStreamFlatMapper<IN, OUT, APP, RESP> mapper) {
        addSyncInstruction(false,
                (state, transport, app) -> mapper.handle(new WebSocketContext<>((IN) state, (WebSocketSession<RESP>) transport, (APP) app)),
                false);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public WebSocketStream<IN, APP, RESP> peek(final WebSocketStreamPeeker<IN, APP, RESP> peeker) {
        return map(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    public WebSocketStream<IN, APP, RESP> blockingPeek(final WebSocketStreamBlockingPeeker<IN> peeker) {
        return blockingMap(ctx -> {
            peeker.handle(ctx);
            return ctx.in();
        });
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketStream<OUT, APP, RESP> blockingMap(final WebSocketStreamBlockingMapper<IN, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketStream<OUT, APP, RESP> blockingFlatMap(final WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        addSyncInstruction(true,
                (state, transport, app) -> mapper.handle(new WebSocketBlockingContext<>((IN) state)),
                false);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> asyncMap(final WebSocketStreamAsyncMapper<IN, OUT, APP, RESP> handler) {
        return asyncMap(handler, AsyncMapConfig.defaultConfig());
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketStream<OUT, APP, RESP> asyncMap(final WebSocketStreamAsyncMapper<IN, OUT, APP, RESP> handler, final AsyncMapConfig config) {
        addAsyncMapInstruction(false,
                (state, transport, app, par) -> handler.handle(new WebSocketAsyncContext<>((IN) state, (WebSocketSession<RESP>) transport, (APP) app, par)),
                config);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    public <OUT> WebSocketStream<OUT, APP, RESP> asyncBlockingMap(final WebSocketStreamAsyncBlockingMapper<IN, OUT> handler) {
        return asyncBlockingMap(handler, AsyncMapConfig.defaultConfig());
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketStream<OUT, APP, RESP> asyncBlockingMap(final WebSocketStreamAsyncBlockingMapper<IN, OUT> handler, final AsyncMapConfig config) {
        addAsyncMapInstruction(true,
                (state, transport, app, par) -> handler.handle(new WebSocketBlockingAsyncContext<>((IN) state, par)),
                config);
        return new WebSocketStream<>(instructionChain, applicationState, pendingAsyncResponses);
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketPipeline<OUT> complete(final WebSocketStreamFlatMapper<IN, OUT, APP, RESP> mapper) {
        addSyncInstruction(false,
                (state, transport, app) -> mapper.handle(new WebSocketContext<>((IN) state, (WebSocketSession<RESP>) transport, (APP) app)),
                true);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<IN> complete() {
        return complete((WebSocketStreamFlatMapper<IN, IN, APP, RESP>) ctx -> WebSocketResult.success(ctx.in()));
    }

    public WebSocketPipeline<Void> completeWithNoResponse() {
        return new WebSocketPipeline<>(instructionChain, applicationState, false);
    }

    @SuppressWarnings("unchecked")
    public <OUT> WebSocketPipeline<OUT> blockingComplete(final WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        addSyncInstruction(true,
                (state, transport, app) -> mapper.handle(new WebSocketBlockingContext<>((IN) state)),
                true);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }
}
