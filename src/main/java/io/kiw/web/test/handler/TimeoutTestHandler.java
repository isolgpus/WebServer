package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;

import io.kiw.web.infrastructure.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TimeoutTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public RequestPipeline<ThrowResponse> handle(HttpResponseStream<ThrowRequest, MyApplicationState> e) {
        return e
            .blockingMap(ctx ->
            {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
                return ctx.in();

            }).complete(ctx ->
                HttpResult.error(ErrorStatusCode.INTERNAL_SERVER_ERROR, new ErrorMessageResponse("should not have got here")));
    }
}
