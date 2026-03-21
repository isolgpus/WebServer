package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.http.EmptyRequest;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class GetTestFilterHandler extends VertxJsonRoute<EmptyRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public RequestPipeline<TestFilterResponse> handle(HttpStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
