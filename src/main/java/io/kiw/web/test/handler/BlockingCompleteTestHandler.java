package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import static io.kiw.web.infrastructure.HttpResult.success;

public class BlockingCompleteTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpResponseStream<BlockingRequest, MyApplicationState> httpResponseStream) {
        return
            httpResponseStream
                .map(ctx -> ctx.in().numberToMultiply)
                .blockingComplete(ctx -> success(new BlockingTestResponse(ctx.in() * 2)));
    }
}
