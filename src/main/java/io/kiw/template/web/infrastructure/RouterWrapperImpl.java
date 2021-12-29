package io.kiw.template.web.infrastructure;

import io.vertx.ext.web.Router;

public class RouterWrapperImpl implements RouterWrapper {
    private final Router router;

    public RouterWrapperImpl(Router router) {
        this.router = router;
    }

    @Override
    public void route(String path, Method method, String consumes, String produces, ContextHandler contextHandler) {
        router.route(method.getVertxMethod(), path).consumes(consumes).produces(produces)
                .handler(ctx -> contextHandler.handle(new VertxContextImpl(ctx)));
    }
}
