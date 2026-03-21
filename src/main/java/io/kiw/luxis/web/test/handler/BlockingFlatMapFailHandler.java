package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;
import io.kiw.luxis.web.test.MyApplicationState;

public class BlockingFlatMapFailHandler extends VertxJsonRoute<BlockingRequest, BlockingTestResponse, MyApplicationState> {

    @Override
    public RequestPipeline<BlockingTestResponse> handle(HttpStream<BlockingRequest, MyApplicationState> httpStream) {
        return httpStream
            .<Integer>blockingFlatMap(ctx -> HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("blocking flat map failed")))
            .complete(ctx -> HttpResult.success(new BlockingTestResponse(ctx.in())));
    }
}
