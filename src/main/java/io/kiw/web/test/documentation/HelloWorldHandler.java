package io.kiw.web.test.documentation;

import io.kiw.web.infrastructure.Flow;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.VertxJsonRoute;

public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, HelloWorldState> {


    @Override
    public Flow<HelloWorldResponse> handle(HttpResponseStream<HelloWorldRequest, HelloWorldState> e) {
        return e.complete((request, httpContext, applicationState) -> {
            return HttpResult.success(new HelloWorldResponse());
        });
    }
}
