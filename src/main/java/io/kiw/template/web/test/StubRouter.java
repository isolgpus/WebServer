package io.kiw.template.web.test;

import io.kiw.template.web.infrastructure.ContextHandler;
import io.kiw.template.web.infrastructure.Method;
import io.kiw.template.web.infrastructure.RouterWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class StubRouter implements RouterWrapper {
    private Map<RouteKey, ContextHandler> routes = new LinkedHashMap<>();

    @Override
    public void route(String path, Method method, String consumes, String provides, ContextHandler contextHandler) {
        routes.put(new RouteKey(path, method), contextHandler);
    }

    StubHttpResponse handle(StubRequest stubRequest, Method post) {
        StubVertxContext context = new StubVertxContext(stubRequest.body, stubRequest.queryParams, stubRequest.headers, stubRequest.cookies);
        this.routes.get(new RouteKey(stubRequest.path, post)).handle(context);
        return context.getResponse();
    }
}
