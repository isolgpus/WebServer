package io.kiw.web.test.handler;

import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.handler.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import static io.kiw.web.http.HttpResult.success;

public class BlockingCompleteTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return
            httpStream
                .map(ctx -> ctx.in().numberToMultiply)
                .blockingComplete(ctx -> success(new BlockingTestResponse(ctx.in() * 2)));
    }
}
