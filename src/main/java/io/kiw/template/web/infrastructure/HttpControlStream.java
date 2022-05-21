package io.kiw.template.web.infrastructure;

import java.util.List;

public class HttpControlStream<IN, APP> {
    private final List<MapInstruction> instructionChain;
    private final boolean canFinishSuccessfully;
    private final APP applicationState;

    public HttpControlStream(List<MapInstruction> instructionChain, boolean canFinishSuccessfully, APP applicationState) {
        this.instructionChain = instructionChain;
        this.canFinishSuccessfully = canFinishSuccessfully;
        this.applicationState = applicationState;
    }

    public <OUT> HttpControlStream<OUT, APP> map(HttpControlStreamMapper<IN, OUT, APP> flowHandler)
    {

        return flatMap((in, httpContext, application) -> HttpResult.success(flowHandler.handle(in, httpContext, application)));
    }

    public <OUT> HttpControlStream<OUT, APP> flatMap(HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, false));
        return new HttpControlStream<>(instructionChain, canFinishSuccessfully, applicationState);
    }


    public <OUT> HttpControlStream<OUT, APP> blockingMap(HttpControlStreamBlockingMapper<IN, OUT> flowHandler)
    {

        return blockingFlatMap((in, httpContext) -> HttpResult.success(flowHandler.handle(in, httpContext)));
    }

    public <OUT> HttpControlStream<OUT, APP> blockingFlatMap(HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(true,
            (HttpControlStreamFlatMapper<IN, OUT, APP>) (request, httpContext, applicationState) ->
                httpControlStreamFlatMapper.handle(request, httpContext), false));
        return new HttpControlStream<>(instructionChain, canFinishSuccessfully, applicationState);
    }

    public <OUT extends JsonResponse> Flow<OUT> complete(HttpControlStreamFlatMapper<IN, OUT, APP> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(false, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new Flow<>(instructionChain, applicationState);
    }

    public <OUT extends JsonResponse> Flow<OUT>  blockingComplete(HttpControlStreamBlockingFlatMapper<IN, OUT> httpControlStreamFlatMapper)
    {
        instructionChain.add(new MapInstruction<>(true, httpControlStreamFlatMapper, canFinishSuccessfully));
        return new Flow<>(instructionChain, applicationState);
    }
}
