package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpResponseStream;
import io.kiw.template.web.infrastructure.VertxJsonRoute;
import io.kiw.template.web.test.MyApplicationState;

import static io.kiw.template.web.infrastructure.HttpResult.success;

public class BlockingCompleteTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public Flow<BlockingTestResponse> handle(HttpResponseStream<BlockingRequest, MyApplicationState> httpResponseStream) {
        return
            httpResponseStream
                .map((blockingRequest, httpContext, applicationState) -> blockingRequest.numberToMultiply)
                .blockingComplete((numberToMultiply, httpContext) -> success(new BlockingTestResponse(numberToMultiply * 2)));
    }
}
