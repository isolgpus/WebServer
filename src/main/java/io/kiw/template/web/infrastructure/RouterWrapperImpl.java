package io.kiw.template.web.infrastructure;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class RouterWrapperImpl extends RouterWrapper {
    private final Router router;

    public RouterWrapperImpl(Router router) {
        this.router = router;
    }

    @Override
    public void route(String path, Method method, String consumes, String produces, Flow flow) {
        Route route = router.route(method.getVertxMethod(), path).consumes(consumes).produces(produces);


        for (Object what : flow.getApplicationInstructions()) {
            FlowInstruction applicationInstruction = (FlowInstruction) what;
            if(applicationInstruction.isBlocking)
            {
                route.blockingHandler(ctx -> handle(applicationInstruction, new VertxContextImpl(ctx)));
            }
            else
            {
                route.handler(ctx -> handle(applicationInstruction, new VertxContextImpl(ctx)));
            }
        }


    }

}
