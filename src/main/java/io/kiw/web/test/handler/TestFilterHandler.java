package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;


import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.handler.VertxJsonRoute;

public class TestFilterHandler extends VertxJsonRoute<TestFilterRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public RequestPipeline<TestFilterResponse> handle(HttpStream<TestFilterRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
