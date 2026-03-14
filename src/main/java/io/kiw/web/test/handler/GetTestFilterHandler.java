package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;


import io.kiw.web.infrastructure.*;

public class GetTestFilterHandler extends VertxJsonRoute<EmptyRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public RequestPipeline<TestFilterResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
