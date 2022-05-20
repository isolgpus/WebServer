package io.kiw.template.web.infrastructure;

import io.kiw.template.web.test.handler.RouteConfig;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.function.Consumer;

public class RouterWrapperImpl extends RouterWrapper {
    private final Router router;
    private final int defaultTimeoutMillis;

    public RouterWrapperImpl(Router router, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler) {
        super(exceptionHandler);
        this.router = router;
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

    @Override
    public void route(String path, Method method, String consumes, String produces, Flow flow, RouteConfig routeConfig) {
        Route route = router.route(method.getVertxMethod(), path).consumes(consumes).produces(produces);
        int timeout = routeConfig.timeoutInMillis.orElse(defaultTimeoutMillis);

        route.handler(new VertxTimeoutHandler(timeout));

        for (Object what : flow.getApplicationInstructions()) {
            MapInstruction applicationInstruction = (MapInstruction) what;
            if(applicationInstruction.isBlocking)
            {
                route.blockingHandler(ctx -> handle(applicationInstruction, new VertxContextImpl(ctx), null));
            }
            else
            {
                route.handler(ctx -> handle(applicationInstruction, new VertxContextImpl(ctx), flow.getApplicationState()));
            }
        }


    }

}
