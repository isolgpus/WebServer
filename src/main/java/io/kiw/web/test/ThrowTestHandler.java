package io.kiw.web.test;

import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.HttpResponseStream;
import io.kiw.web.infrastructure.HttpResult;
import io.kiw.web.infrastructure.VertxJsonRoute;

public class ThrowTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public RequestPipeline<ThrowResponse> handle(HttpResponseStream<ThrowRequest, MyApplicationState> e) {
        return e.map(ctx -> ctx.in().where).map(ctx -> {
            if("map".equals(ctx.in()))
            {
                throw new RuntimeException("app error in map");
            }
            return ctx.in();
        }).blockingMap(ctx -> {
            if("blocking".equals(ctx.in()))
            {
                throw new RuntimeException("app error in blocking");
            }
            return ctx.in();
        }).complete(ctx -> {
            if("complete".equals(ctx.in()))
            {
                throw new RuntimeException("app error in complete");
            }
            return HttpResult.success(new ThrowResponse());
        });
    }
}
