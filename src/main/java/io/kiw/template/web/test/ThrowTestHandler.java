package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.HttpControlStream;
import io.kiw.template.web.infrastructure.HttpResult;
import io.kiw.template.web.infrastructure.VertxJsonRoute;

public class ThrowTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public Flow<ThrowResponse> handle(HttpControlStream<ThrowRequest, MyApplicationState> e) {
        return e.map((request, b, c) -> request.where).map((where,b,c) -> {
            if("map".equals(where))
            {
                throw new RuntimeException("app error");
            }
            return where;
        }).blockingMap((where, httpContext) -> {
            if("blocking".equals(where))
            {
                throw new RuntimeException("app error");
            }
            return where;
        }).complete((where, httpContext, app) -> {
            if("complete".equals(where))
            {
                throw new RuntimeException("app error");
            }
            return HttpResult.success(new ThrowResponse());
        });
    }
}
