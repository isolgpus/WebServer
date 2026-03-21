package io.kiw.web.test.handler;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;
import io.kiw.web.test.MyApplicationState;

public class ErrorFilter implements VertxJsonFilter<MyApplicationState> {

    @Override
    public RequestPipeline<Void> handle(HttpStream<Void, MyApplicationState> e) {
        return e.complete(ctx -> HttpResult.error(ErrorStatusCode.UNAUTHORIZED, new ErrorMessageResponse("filter blocked")));
    }
}
