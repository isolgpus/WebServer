package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.Flow;
import io.kiw.template.web.infrastructure.MapInstruction;
import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RouterWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class StubRouter extends RouterWrapper {
    private Map<RouteKey, Flow> routes = new LinkedHashMap<>();

    @Override
    public void route(String path, Method method, String consumes, String provides, Flow flow) {
        routes.put(new RouteKey(path, method), flow);
    }

    StubHttpResponse handle(StubRequest stubRequest, Method post) {
        StubVertxContext context = new StubVertxContext(stubRequest.body, stubRequest.queryParams, stubRequest.headers, stubRequest.cookies);
        Flow flow = this.routes.get(new RouteKey(stubRequest.path, post));
        for (Object what : flow.getApplicationInstructions()) {
            MapInstruction applicationInstruction = (MapInstruction)what;
            this.handle(applicationInstruction, context);
            if(context.hasFinished())
            {
                break;
            }
        }

        return context.getResponse();
    }
}
