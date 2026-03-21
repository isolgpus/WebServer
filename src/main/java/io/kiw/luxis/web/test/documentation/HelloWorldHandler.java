package io.kiw.luxis.web.test.documentation;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;

public class HelloWorldHandler extends VertxJsonRoute<HelloWorldRequest, HelloWorldResponse, AppState> {


    @Override
    public RequestPipeline<HelloWorldResponse> handle(HttpStream<HelloWorldRequest, AppState> e) {
        return e.complete(ctx -> HttpResult.success(new HelloWorldResponse()));
    }
}
