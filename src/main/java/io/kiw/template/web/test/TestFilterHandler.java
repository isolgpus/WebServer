package io.kiw.template.web.test;


import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

public class TestFilterHandler extends VertxJsonRoute<TestFilterRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public Flow<TestFilterResponse> handle(HttpControlStream<TestFilterRequest, MyApplicationState> e) {
        return e.complete((request, httpContext, myApplicationState) ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
