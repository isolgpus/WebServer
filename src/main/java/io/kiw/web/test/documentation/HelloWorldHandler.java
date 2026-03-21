package io.kiw.web.test.documentation;

import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.handler.VertxJsonRoute;

public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, HelloWorldState> {


    @Override
    public RequestPipeline<HelloWorldResponse> handle(HttpStream<HelloWorldRequest, HelloWorldState> e) {
        return e.complete(ctx -> HttpResult.success(new HelloWorldResponse()));
    }
}
