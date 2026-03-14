package io.kiw.web.test;

import io.kiw.web.infrastructure.*;

public class StateTestHandler extends VertxJsonRoute<EmptyRequest, StateResponse, MyApplicationState> {

    @Override
    public RequestPipeline<StateResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> e) {
        return e.complete((request, httpContext, myApplicationState) ->
            HttpResult.success(new StateResponse(myApplicationState.getLongValue())));
    }
}
