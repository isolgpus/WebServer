package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.internal.WebSocketMapInstruction;
import io.kiw.luxis.web.internal.WebSocketPipeline;
import io.kiw.luxis.web.websocket.WebSocketResult;

import java.util.List;

public class WebSocketStream<IN, APP> {
    private final List<WebSocketMapInstruction> instructionChain;
    private final APP applicationState;

    public WebSocketStream(List<WebSocketMapInstruction> instructionChain, APP applicationState) {
        this.instructionChain = instructionChain;
        this.applicationState = applicationState;
    }

    public <OUT> WebSocketStream<OUT, APP> map(WebSocketStreamMapper<IN, OUT, APP> flowHandler) {
        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP> flatMap(WebSocketStreamFlatMapper<IN, OUT, APP> mapper) {
        WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(false, mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }

        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> blockingMap(WebSocketStreamBlockingMapper<IN, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP> blockingFlatMap(WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(true, mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> asyncMap(WebSocketStreamAsyncMapper<IN, OUT, APP> flowHandler) {
        return asyncFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> WebSocketStream<OUT, APP> asyncFlatMap(WebSocketStreamAsyncFlatMapper<IN, OUT, APP> mapper) {
        WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> asyncBlockingMap(WebSocketStreamAsyncBlockingMapper<IN, OUT> flowHandler) {
        return asyncBlockingFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> WebSocketStream<OUT, APP> asyncBlockingFlatMap(WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> mapper) {
        WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(mapper, false);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketPipeline<OUT> complete(WebSocketStreamFlatMapper<IN, OUT, APP> mapper) {
        WebSocketMapInstruction<IN, OUT, APP> e = new WebSocketMapInstruction<>(false, mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public WebSocketPipeline<IN> complete() {
        WebSocketMapInstruction<IN, IN, APP> e = new WebSocketMapInstruction<>(false,
                (WebSocketStreamFlatMapper<IN, IN, APP>) ctx -> WebSocketResult.success(ctx.in()), true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketPipeline<OUT> blockingComplete(WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        WebSocketMapInstruction<IN, OUT, Object> e = new WebSocketMapInstruction<>(true, mapper, true);
        if (!instructionChain.isEmpty()) {
            instructionChain.getLast().setNext(e);
        }
        instructionChain.add(e);
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }
}
