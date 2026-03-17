package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.HttpStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.HttpSuccessResponse;
import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

public class StatusCodeTestHandler extends VertxJsonRoute<StatusCodeRequest, HttpSuccessResponse<StatusCodeResponse>, MyApplicationState> {

    @Override
    public RequestPipeline<HttpSuccessResponse<StatusCodeResponse>> handle(HttpStream<StatusCodeRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new StatusCodeResponse(ctx.in().value), ctx.in().statusCode));
    }
}
