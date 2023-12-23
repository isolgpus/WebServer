package io.kiw.web.test;

import io.kiw.web.infrastructure.Flow;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.VertxJsonRoute;

public class ThrowTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public Flow<ThrowResponse> handle(HttpResponseStream<ThrowRequest, MyApplicationState> e) {
        return e.map((request, b, c) -> request.where).map((where,b,c) -> {
            if("map".equals(where))
            {
                throw new RuntimeException("app error in map");
            }
            return where;
        }).blockingMap((where, httpContext) -> {
            if("blocking".equals(where))
            {
                throw new RuntimeException("app error in blocking");
            }
            return where;
        }).complete((where, httpContext, app) -> {
            if("complete".equals(where))
            {
                throw new RuntimeException("app error in complete");
            }
            return HttpResult.success(new ThrowResponse());
        });
    }
}
