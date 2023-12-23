package io.kiw.template.web.test.documentation;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpResponseStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, HelloWorldState> {


    @Override
    public Flow<HelloWorldResponse> handle(HttpResponseStream<HelloWorldRequest, HelloWorldState> e) {
        return e.complete((request, httpContext, applicationState) -> {
            return HttpResult.success(new HelloWorldResponse());
        });
    }
}
