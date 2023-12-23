package io.kiw.template.web.test.handler;

import io.kiw.template.web.infrastructure.*;
import io.kiw.template.web.test.MyApplicationState;

import static io.kiw.result.Result.success;


public class FailingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public Flow<BlockingTestResponse> handle(HttpResponseStream<BlockingRequest, MyApplicationState> httpResponseStream) {
        return
            httpResponseStream
                .map((blockingRequest, httpContext, myApplicationState) -> blockingRequest.numberToMultiply)
                .flatMap((numberToMultiply, httpContext, myApplicationState) -> {
                    if(2 * 2 == 4)
                    {
                        return HttpResult.error(400, new ErrorMessageResponse("intentionally failed"));
                    }
                    else
                    {
                        return success(4);
                    }
                })
                // should never reach this
                .complete((multipliedNumber, httpContext, myApplicationState) -> success(new BlockingTestResponse(multipliedNumber)));
    }
}
