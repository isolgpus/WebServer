package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.test.MyApplicationState;


import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;

public class GetTestFilterHandler extends VertxJsonRoute<EmptyRequest, TestFilterResponse, MyApplicationState> {

    @Override
    public RequestPipeline<TestFilterResponse> handle(HttpStream<EmptyRequest, MyApplicationState> e) {
        return e.complete(ctx ->
            HttpResult.success(new TestFilterResponse("hit handler")));
    }
}
