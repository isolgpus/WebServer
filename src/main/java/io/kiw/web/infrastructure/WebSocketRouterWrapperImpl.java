package io.kiw.web.infrastructure;

import io.vertx.core.Vertx;

public class WebSocketRouterWrapperImpl implements WebSocketRouterWrapper {
    private final Vertx vertx;

    public WebSocketRouterWrapperImpl(Vertx vertx) {
        this.vertx = vertx;
    }
}
