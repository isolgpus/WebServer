package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;

import static io.kiw.result.Result.success;


public class FailingTestHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return
            httpStream
                .map(ctx -> ctx.in().numberToMultiply)
                .flatMap(ctx -> {
                    if(2 * 2 == 4)
                    {
                        return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("intentionally failed"));
                    }
                    else
                    {
                        return success(4);
                    }
                })
                // should never reach this
                .complete(ctx -> success(new BlockingTestResponse(ctx.in())));
    }
}
