package io.kiw.web.test;


import io.kiw.web.infrastructure.*;

public class GetTestFilterHandler extends VertxJsonRoute<EmptyRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public RequestPipeline<TestFilterResponse> handle(HttpResponseStream<EmptyRequest, MyApplicationState> e) {
        return e.complete((request, context, app) ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
