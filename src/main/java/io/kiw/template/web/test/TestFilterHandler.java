package io.kiw.template.web.test;


import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

public class TestFilterHandler extends VertxJsonRoute<TestFilterRequest, TestFilterResponse> {

    @Override
    public Flow<TestFilterResponse> handle(HttpControlStream<TestFilterRequest> e) {
        return e.complete((request, httpContext) ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
