package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.internal.ender.Ender;

import java.util.List;

public class HttpMapStream<IN, APP> {
    protected final List<MapInstruction> instructionChain;
    protected final boolean canFinishSuccessfully;
    protected final APP applicationState;
    protected final Ender ender;

    public HttpMapStream(final List<MapInstruction> instructionChain, final boolean canFinishSuccessfully, final APP applicationState, final Ender ender) {
        this.instructionChain = instructionChain;
        this.canFinishSuccessfully = canFinishSuccessfully;
        this.applicationState = applicationState;
        this.ender = ender;
    }

    public <OUT> HttpMapStream<OUT, APP> map(final HttpControlStreamMapper<IN, OUT, APP> flowHandler) {

        return flatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> flatMap(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }


    public <OUT> HttpMapStream<OUT, APP> blockingMap(final HttpControlStreamBlockingMapper<IN, OUT> flowHandler) {

        return blockingFlatMap(ctx -> Result.success(flowHandler.handle(ctx)));
    }

    public <OUT> HttpMapStream<OUT, APP> blockingFlatMap(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncMap(final HttpControlStreamAsyncMapper<IN, OUT, APP> flowHandler) {
        return asyncFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> HttpMapStream<OUT, APP> asyncFlatMap(final HttpControlStreamAsyncFlatMapper<IN, OUT, APP> mapper) {
        instructionChain.add(new MapInstruction<>(mapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingMap(final HttpControlStreamAsyncBlockingMapper<IN, OUT> flowHandler) {
        return asyncBlockingFlatMap(ctx -> flowHandler.handle(ctx).thenApply(Result::success));
    }

    public <OUT> HttpMapStream<OUT, APP> asyncBlockingFlatMap(final HttpControlStreamAsyncBlockingFlatMapper<IN, OUT> mapper) {
        instructionChain.add(new MapInstruction<>(mapper, false));
        return new HttpMapStream<>(instructionChain, canFinishSuccessfully, applicationState, ender);
    }

    public <OUT> RequestPipeline<OUT> complete(final HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }

    public <OUT> RequestPipeline<OUT> complete() {
        return complete(a -> HttpResult.success());
    }


    public <OUT> RequestPipeline<OUT>  blockingComplete(final HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper) {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new RequestPipeline<>(instructionChain, applicationState, ender);
    }
}
