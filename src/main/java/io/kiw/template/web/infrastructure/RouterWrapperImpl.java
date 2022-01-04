package io.kiw.template.web.infrastructure;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.function.Consumer;

public class RouterWrapperImpl extends RouterWrapper {
    private final Router router;

    public RouterWrapperImpl(Router router, Consumer<Throwable> errorHandler) {
        super(errorHandler);
        this.router = router;
    }

    @Override
    public void route(String path, Method method, String consumes, String produces, Flow flow) {
        Route route = router.route(method.getVertxMethod(), path).consumes(consumes).produces(produces);


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
