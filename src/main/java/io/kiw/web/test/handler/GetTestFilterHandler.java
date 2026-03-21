package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;


import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;
import io.kiw.web.internal.*;

public class GetTestFilterHandler extends VertxJsonRoute<EmptyRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public RequestPipeline<TestFilterResponse> handle(HttpStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
