package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.test.MyApplicationState;

public class BlockingFlatMapFailHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return httpStream
            .<Integer>blockingFlatMap(ctx -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("blocking flat map failed")))
            .complete(ctx -> HttpResult.success(new BlockingTestResponse(ctx.in())));
    }
}
