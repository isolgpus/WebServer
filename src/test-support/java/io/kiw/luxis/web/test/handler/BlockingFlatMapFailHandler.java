package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.JsonHandler;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class BlockingFlatMapFailHandler extends JsonHandler<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(final HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return httpStream
                .<Integer>blockingFlatMap(ctx -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("blocking flat map failed")))
                .complete(ctx -> HttpResult.success(new BlockingTestResponse(ctx.in())));
    }
}
