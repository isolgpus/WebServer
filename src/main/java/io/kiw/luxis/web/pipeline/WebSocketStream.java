package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.internal.WebSocketMapInstruction;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.validation.WebSocketValidator;
import io.kiw.luxis.web.websocket.WebSocketResult;

import java.util.List;
import java.util.function.Consumer;

public class WebSocketStream<IN, APP> {
    private final List<WebSocketMapInstruction> instructionChain;
    private final APP applicationState;

    public WebSocketStream(final List<WebSocketMapInstruction> instructionChain, final APP applicationState) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
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
        return new WebSocketStream<>(instructionChain, applicationState);
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
        return new WebSocketStream<>(instructionChain, applicationState);
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
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> asyncMap(final WebSocketStreamAsyncMapper<IN, OUT, APP> flowHandler) {
        return asyncFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> WebSocketStream<OUT, APP> asyncFlatMap(final WebSocketStreamAsyncFlatMapper<IN, OUT, APP> mapper) {
        final WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> asyncBlockingMap(final WebSocketStreamAsyncBlockingMapper<IN, OUT> flowHandler) {
        return asyncBlockingFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> WebSocketStream<OUT, APP> asyncBlockingFlatMap(final WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> mapper) {
        final WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState);
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
