package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TimeoutTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public Flow<ThrowResponse> handle(HttpResponseStream<ThrowRequest, MyApplicationState> e) {
        return e
            .blockingMap((request, httpContext) ->
            {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
                return request;

            }).complete((message, httpContext, app) ->
                HttpResult.error(500, new ErrorMessageResponse("should not have got here")));
    }
}
