package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

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
