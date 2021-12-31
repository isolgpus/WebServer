package io.kiw.template.web.infrastructure;

import java.util.List;

public class HttpControlStream<IN> {
    private final List<MapInstruction> instructionChain;
    private final boolean canFinishSuccessfully;

    public HttpControlStream(List<MapInstruction> instructionChain, boolean canFinishSuccessfully) {
        this.instructionChain = instructionChain;
        this.canFinishSuccessfully = canFinishSuccessfully;
    }

    public <OUT> HttpControlStream<OUT> map(HttpControlStreamMapper<IN, OUT> flowHandler)
    {

        return flatMap((in, httpContext) -> HttpResult.success(flowHandler.handle(in, httpContext)));
    }

    public <OUT> HttpControlStream<OUT> flatMap(HttpControlStreamFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, false));
        return new HttpControlStream<>(instructionChain, canFinishSuccessfully);
    }


    public <OUT> HttpControlStream<OUT> blockingMap(HttpControlStreamMapper<IN, OUT> flowHandler)
    {

        return blockingFlatMap((in, httpContext) -> HttpResult.success(flowHandler.handle(in, httpContext)));
    }

    public <OUT> HttpControlStream<OUT> blockingFlatMap(HttpControlStreamFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, false));
        return new HttpControlStream<>(instructionChain, canFinishSuccessfully);
    }

    public <OUT extends JsonResponse> Flow<OUT> complete(HttpControlStreamFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new Flow<>(instructionChain);
    }
}
