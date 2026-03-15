package io.kiw.web.test.handler;

import io.kiw.web.infrastructure.*;
import io.kiw.web.test.MyApplicationState;

public class ErrorFilter implements VertxJsonFilter<MyApplicationState> {

    @Override
    public RequestPipeline handle(HttpResponseStream<Void, MyApplicationState> e) {
        return e.complete(ctx -> HttpResult.error(401, new ErrorMessageResponse("filter blocked")));
    }
}
