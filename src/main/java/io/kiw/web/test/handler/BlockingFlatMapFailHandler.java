package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;

public class BlockingFlatMapFailHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpResponseStream<BlockingRequest, MyApplicationState> httpResponseStream) {
        return httpResponseStream
            .<Integer>blockingFlatMap(ctx -> HttpResult.error(400, new ErrorMessageResponse("blocking flat map failed")))
            .complete(ctx -> HttpResult.success(new BlockingTestResponse(ctx.in())));
    }
}
