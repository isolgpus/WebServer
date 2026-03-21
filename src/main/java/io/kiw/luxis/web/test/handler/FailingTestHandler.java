package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.test.MyApplicationState;

import static io.kiw.luxis.result.Result.success;


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
