package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class BlockingFlatMapFailHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return httpStream
            .<Integer>blockingFlatMap(ctx -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("blocking flat map failed")))
            .complete(ctx -> HttpResult.success(new BlockingTestResponse(ctx.in())));
    }
}
