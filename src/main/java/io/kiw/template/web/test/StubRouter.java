package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.*;

import java.util.LinkedHashMap;
import java.util.List;
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
        for (FlowInstruction applicationInstruction : flow.getApplicationInstructions()) {
            this.handle(applicationInstruction, context);
            if(context.hasFinished())
            {
                break;
            }
        }

        return context.getResponse();
    }
}
