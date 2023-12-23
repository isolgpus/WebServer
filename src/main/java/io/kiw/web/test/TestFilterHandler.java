package io.kiw.web.test;


import io.kiw.web.infrastructure.Flow;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.VertxJsonRoute;

public class TestFilterHandler extends VertxJsonRoute<TestFilterRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public Flow<TestFilterResponse> handle(HttpResponseStream<TestFilterRequest, MyApplicationState> e) {
        return e.complete((request, context, app) ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
