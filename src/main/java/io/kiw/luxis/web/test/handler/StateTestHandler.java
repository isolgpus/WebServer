package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.EmptyRequest;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class StateTestHandler extends VertxJsonRoute<EmptyRequest, StateResponse, MyApplicationState> {

    @Override
    public RequestPipeline<StateResponse> handle(HttpStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new StateResponse(ctx.app().getLongValue())));
    }
}
