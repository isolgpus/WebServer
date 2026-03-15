package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.VertxJsonRoute;
import io.kiw.web.test.MyApplicationState;

public class StatusCodeTestHandler extends VertxJsonRoute<StatusCodeRequest, StatusCodeResponse, MyApplicationState> {

    @Override
    public RequestPipeline<StatusCodeResponse> handle(HttpResponseStream<StatusCodeRequest, MyApplicationState> e) {
        return e.complete(ctx -> {
            ctx.http().setStatusCode(ctx.in().statusCode);
            return HttpResult.success(new StatusCodeResponse(ctx.in().value));
        });
    }
}
