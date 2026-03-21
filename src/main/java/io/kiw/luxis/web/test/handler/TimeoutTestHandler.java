package io.kiw.luxis.web.test.handler;

import io.kiw.luxis.web.test.MyApplicationState;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;
import io.kiw.luxis.web.internal.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TimeoutTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public RequestPipeline<ThrowResponse> handle(HttpStream<ThrowRequest, MyApplicationState> e) {
        return e
            .blockingMap(ctx ->
            {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
                return ctx.in();

            }).complete(ctx ->
                HttpResult.error(ErrorStatusCode.INTERNAL_SERVER_ERROR, new ErrorMessageResponse("should not have got here")));
    }
}
