package io.kiw.web.test.handler;

import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.http.HttpSuccessResponse;
import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.handler.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

public class StatusCodeTestHandler extends VertxJsonRoute<StatusCodeRequest, HttpSuccessResponse<StatusCodeResponse>, MyApplicationState> {

    @Override
    public RequestPipeline<HttpSuccessResponse<StatusCodeResponse>> handle(HttpStream<StatusCodeRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new StatusCodeResponse(ctx.in().value), ctx.in().statusCode));
    }
}
