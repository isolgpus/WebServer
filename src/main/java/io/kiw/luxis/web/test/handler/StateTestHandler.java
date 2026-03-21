package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.test.MyApplicationState;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;

public class StateTestHandler extends VertxJsonRoute<EmptyRequest, StateResponse, MyApplicationState> {

    @Override
    public RequestPipeline<StateResponse> handle(HttpStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new StateResponse(ctx.app().getLongValue())));
    }
}
