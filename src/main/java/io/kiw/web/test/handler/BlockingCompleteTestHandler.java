package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.Flow;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

import static io.kiw.web.infrastructure.HttpResult.success;

public class BlockingCompleteTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public Flow<BlockingTestResponse> handle(HttpResponseStream<BlockingRequest, MyApplicationState> httpResponseStream) {
        return
            httpResponseStream
                .map((blockingRequest, httpContext, applicationState) -> blockingRequest.numberToMultiply)
                .blockingComplete((numberToMultiply, httpContext) -> success(new BlockingTestResponse(numberToMultiply * 2)));
    }
}
