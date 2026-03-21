package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class VertxTimeoutHandler implements Handler<RoutingContext> {

    private final int timeout;

    public VertxTimeoutHandler(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void handle(RoutingContext ctx) {
        long tid = ctx.vertx().setTimer(timeout, (t) -> {
            ctx.data().put("CONTEXT_DEAD", true);
            ctx.end("{\"message\":\"Service Unavailable\"}");
        });

        ctx.addBodyEndHandler((v) -> {
            ctx.vertx().cancelTimer(tid);
        });
        ctx.next();
    }
}
