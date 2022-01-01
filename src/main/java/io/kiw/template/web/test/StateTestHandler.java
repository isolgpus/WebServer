package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.*;

public class StateTestHandler extends VertxJsonRoute<EmptyRequest, StateResponse> {

    private final MyApplicationState myApplicationState;

    public StateTestHandler(MyApplicationState myApplicationState) {

        this.myApplicationState = myApplicationState;
    }

    @Override
    public Flow<StateResponse> handle(HttpControlStream<EmptyRequest> e) {
        return e.complete((request, httpContext) ->
            HttpResult.success(new StateResponse(myApplicationState.getLongValue())));
    }
}
