package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

import static io.kiw.template.web.infrastructure.HttpResult.success;

public class BlockingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse> {

    @Override
    public Flow<BlockingTestResponse> handle(HttpControlStream<BlockingRequest> httpControlStream) {
        return
            httpControlStream
                .map((blockingRequest, httpContext) -> blockingRequest.numberToMultiply)
                .blockingMap((numberToMultiply, httpContext) -> numberToMultiply * 2)
                .complete((multipliedNumber, httpContext) ->
                    success(new BlockingTestResponse(multipliedNumber))
                );
    }
}
