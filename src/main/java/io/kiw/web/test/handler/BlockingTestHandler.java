package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.HttpStream;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import static io.kiw.web.infrastructure.HttpResult.success;

public class BlockingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return
            httpStream
                .map(ctx -> ctx.in().numberToMultiply)
                .blockingMap(ctx -> ctx.in() * 2)
                .complete(ctx -> success(new BlockingTestResponse(ctx.in())));
    }
}
