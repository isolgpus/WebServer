package io.kiw.web.infrastructure;

import io.kiw.result.Result;

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
        instructionChain.add(new WebSocketMapInstruction<>(false, mapper, false));
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> blockingMap(WebSocketStreamBlockingMapper<IN, OUT> flowHandler) {
        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> WebSocketStream<OUT, APP> blockingFlatMap(WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        instructionChain.add(new WebSocketMapInstruction<>(true, mapper, false));
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> asyncMap(WebSocketStreamAsyncMapper<IN, OUT, APP> flowHandler) {
        return asyncFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> WebSocketStream<OUT, APP> asyncFlatMap(WebSocketStreamAsyncFlatMapper<IN, OUT, APP> mapper) {
        instructionChain.add(new WebSocketMapInstruction<>(mapper, false));
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketStream<OUT, APP> asyncBlockingMap(WebSocketStreamAsyncBlockingMapper<IN, OUT> flowHandler) {
        return asyncBlockingFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> WebSocketStream<OUT, APP> asyncBlockingFlatMap(WebSocketStreamAsyncBlockingFlatMapper<IN, OUT> mapper) {
        instructionChain.add(new WebSocketMapInstruction<>(mapper, false));
        return new WebSocketStream<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketPipeline<OUT> complete(WebSocketStreamFlatMapper<IN, OUT, APP> mapper) {
        instructionChain.add(new WebSocketMapInstruction<>(false, mapper, true));
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }

    public <OUT> WebSocketPipeline<OUT> complete() {
        return complete(a -> WebSocketResult.success());
    }

    public <OUT> WebSocketPipeline<OUT> blockingComplete(WebSocketStreamBlockingFlatMapper<IN, OUT> mapper) {
        instructionChain.add(new WebSocketMapInstruction<>(true, mapper, true));
        return new WebSocketPipeline<>(instructionChain, applicationState);
    }
}
