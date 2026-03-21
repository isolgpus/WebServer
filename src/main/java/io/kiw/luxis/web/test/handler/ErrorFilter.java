package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.handler.VertxJsonFilter;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.test.MyApplicationState;

public class ErrorFilter implements VertxJsonFilter<MyApplicationState> {

    @Override
    public RequestPipeline<Void> handle(HttpStream<Void, MyApplicationState> e) {
        return e.complete(ctx -> HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse("filter blocked")));
    }
}
