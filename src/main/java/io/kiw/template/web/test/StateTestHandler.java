package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.*;

public class StateTestHandler extends VertxJsonRoute<EmptyRequest, StateResponse, MyApplicationState> {

    @Override
    public Flow<StateResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> e) {
        return e.complete((request, httpContext, myApplicationState) ->
            HttpResult.success(new StateResponse(myApplicationState.getLongValue())));
    }
}
