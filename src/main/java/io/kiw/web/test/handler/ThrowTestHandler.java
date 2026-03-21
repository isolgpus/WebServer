package io.kiw.web.test.handler;

import io.kiw.web.test.MyApplicationState;

import io.kiw.web.internal.RequestPipeline;
import io.kiw.web.pipeline.HttpStream;
import io.kiw.web.http.HttpResult;
import io.kiw.web.handler.VertxJsonRoute;

import java.util.concurrent.CompletableFuture;

public class ThrowTestHandler extends VertxJsonRoute<ThrowRequest, ThrowResponse, MyApplicationState> {
    @Override
    public RequestPipeline<ThrowResponse> handle(HttpStream<ThrowRequest, MyApplicationState> e) {
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
        }).asyncMap(ctx -> {
            if("asyncMap".equals(ctx.in()))
            {
                throw new RuntimeException("app error in asyncMap");
            }
            return CompletableFuture.completedFuture(ctx.in());
        }).asyncBlockingMap(ctx -> {
            if("asyncBlockingMap".equals(ctx.in()))
            {
                throw new RuntimeException("app error in asyncBlockingMap");
            }
            return CompletableFuture.completedFuture(ctx.in());
        }).complete(ctx -> {
            if("complete".equals(ctx.in()))
            {
                throw new RuntimeException("app error in complete");
            }
            return HttpResult.success(new ThrowResponse());
        });
    }
}
