package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.MessageResponse;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

import static io.kiw.template.web.infrastructure.HttpResult.error;
import static io.kiw.template.web.infrastructure.HttpResult.success;

public class FailingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse> {

    @Override
    public Flow<BlockingTestResponse> handle(HttpControlStream<BlockingRequest> httpControlStream) {
        return
            httpControlStream
                .map((blockingRequest, httpContext) -> blockingRequest.numberToMultiply)
                .flatMap((numberToMultiply, httpContext) -> {
                    if(2 * 2 == 4)
                    {
                        return error(400, new MessageResponse("intentionally failed"));
                    }
                    else
                    {
                        return success(4);
                    }
                })
                // should never reach this
                .complete((multipliedNumber, httpContext) -> success(new BlockingTestResponse(multipliedNumber)));
    }
}
