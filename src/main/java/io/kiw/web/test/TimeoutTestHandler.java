package io.kiw.web.test;

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
                HttpResult.error(500, new ErrorMessageResponse("should not have got here")));
    }
}
