package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;

import io.kiw.web.infrastructure.*;

public class StateTestHandler extends VertxJsonRoute<EmptyRequest, StateResponse, MyApplicationState> {

    @Override
    public RequestPipeline<StateResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new StateResponse(ctx.app().getLongValue())));
    }
}
