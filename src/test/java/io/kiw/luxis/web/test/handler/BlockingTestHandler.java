package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.web.http.HttpResult.success;

public class BlockingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(final HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return
            httpStream
                .map(ctx -> ctx.in().numberToMultiply)
                .blockingMap(ctx -> ctx.in() * 2)
                .complete(ctx -> success(new BlockingTestResponse(ctx.in())));
    }
}
