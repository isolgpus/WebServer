package io.kiw.luxis.web.test.documentation;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;

public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, HelloWorldState> {


    @Override
    public RequestPipeline<HelloWorldResponse> handle(HttpStream<HelloWorldRequest, HelloWorldState> e) {
        return e.complete(ctx -> HttpResult.success(new HelloWorldResponse()));
    }
}
