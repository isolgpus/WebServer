package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.MapInstruction;
import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RouterWrapper;
import io.kiw.template.web.test.handler.RouteConfig;

import java.util.List;
import java.util.function.Consumer;

public class StubRouter extends RouterWrapper {
    private PathMatcher routes = new PathMatcher();

    public StubRouter(Consumer<Exception> exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    public void route(String path, Method method, String consumes, String provides, Flow flow, RouteConfig routeConfig) {
        routes.putRoute(path, method, flow);
    }

    public StubHttpResponse handle(StubRequest stubRequest, Method method) {
        StubVertxContext context = new StubVertxContext(stubRequest.body, stubRequest.queryParams, stubRequest.headers, stubRequest.cookies, stubRequest.fileUploads);
        List<Flow> flows = this.routes.get(stubRequest.path, method);

        for (Flow flow : flows) {
            for (Object what : flow.getApplicationInstructions()) {
                MapInstruction applicationInstruction = (MapInstruction) what;
                this.handle(applicationInstruction, context, flow.getApplicationState());
                if (context.hasFinished()) {
                    break;
                }
            }

        }


        return context.getResponse();
    }
}
