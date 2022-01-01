package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.VertxJsonRoute;
import io.kiw.template.web.test.MyApplicationState;

import static io.kiw.template.web.infrastructure.HttpResult.success;

public class BlockingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public Flow<BlockingTestResponse> handle(HttpControlStream<BlockingRequest, MyApplicationState> httpControlStream) {
        return
            httpControlStream
                .map((blockingRequest, httpContext, applicationState) -> blockingRequest.numberToMultiply)
                .blockingMap((numberToMultiply, httpContext) -> numberToMultiply * 2)
                .complete((multipliedNumber, httpContext, applicationState) ->
                    success(new BlockingTestResponse(multipliedNumber))
                );
    }
}
