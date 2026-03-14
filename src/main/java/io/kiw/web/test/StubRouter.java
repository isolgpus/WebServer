package io.kiw.web.test;

import io.kiw.web.infrastructure.RequestPipeline;
import io.kiw.web.infrastructure.MapInstruction;
import io.kiw.web.infrastructure.Method;
import io.kiw.web.infrastructure.RouterWrapper;
import io.kiw.web.infrastructure.WebSocketRouteHandler;
import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.test.handler.RouteConfig;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StubRouter extends RouterWrapper {
    private PathMatcher routes = new PathMatcher();
    private CorsConfig corsConfig;
    private final List<WebSocketRouteEntry> webSocketRoutes = new ArrayList<>();

    public StubRouter(Consumer<Exception> exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    public void configureCors(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    @Override
    public void route(String path, Method method, String consumes, String provides, RequestPipeline flow, RouteConfig routeConfig) {
        routes.putRoute(path, method, flow);
    }

    @Override
    public void route(String path, String consumes, String provides, RequestPipeline flow, RouteConfig routeConfig) {
        routes.putAllMethodRoute(path, flow);
    }

    public StubHttpResponse handle(StubRequest stubRequest, Method method) {
        if (corsConfig != null && method == Method.OPTIONS) {
            return handlePreflightRequest(stubRequest);
        }

        StubVertxContext context = new StubVertxContext(stubRequest.body, stubRequest.queryParams, stubRequest.headers, stubRequest.cookies, stubRequest.fileUploads);
        PathMatcher.MatchResult matchResult = this.routes.get(stubRequest.path, method);
        context.setPathParams(matchResult.getPathParams());

        for (RequestPipeline flow : matchResult.getFlows()) {
            for (Object what : flow.getApplicationInstructions()) {
                MapInstruction applicationInstruction = (MapInstruction) what;
                if (applicationInstruction.isAsync) {
                    this.handleAsyncBlocking(applicationInstruction, context, flow.getApplicationState(), flow.getEnder());
                } else {
                    this.handle(applicationInstruction, context, flow.getApplicationState(), flow.getEnder());
                }
                if (context.hasFinished()) {
                    break;
                }
            }
        }

        StubHttpResponse response = context.getResponse();
        if (corsConfig != null) {
            addCorsResponseHeaders(stubRequest, response);
        }
        return response;
    }

    private StubHttpResponse handlePreflightRequest(StubRequest stubRequest) {
        String origin = stubRequest.headers.get("Origin");
        if (origin == null || !corsConfig.isOriginAllowed(origin)) {
            return new StubHttpResponse(null).withStatusCode(403);
        }

        String allowOrigin = corsConfig.getAllowedOrigins().contains("*") ? "*" : origin;

        StubHttpResponse response = new StubHttpResponse(null).withStatusCode(204)
            .withHeader("Access-Control-Allow-Origin", allowOrigin);

        if (!corsConfig.getAllowedMethods().isEmpty()) {
            response.withHeader("Access-Control-Allow-Methods",
                String.join(",", corsConfig.getAllowedMethods()));
        }

        if (!corsConfig.getAllowedHeaders().isEmpty()) {
            response.withHeader("Access-Control-Allow-Headers",
                String.join(",", corsConfig.getAllowedHeaders()));
        }

        if (corsConfig.isAllowCredentials()) {
            response.withHeader("Access-Control-Allow-Credentials", "true");
        }

        if (corsConfig.getMaxAgeSeconds() >= 0) {
            response.withHeader("Access-Control-Max-Age", String.valueOf(corsConfig.getMaxAgeSeconds()));
        }

        return response;
    }

    @Override
    protected void webSocketRoute(String path, WebSocketRouteHandler<?, ?, ?> handler) {
        webSocketRoutes.add(new WebSocketRouteEntry(path, handler));
    }

    public StubWebSocketClient webSocket(StubRequest stubRequest) {
        for (WebSocketRouteEntry entry : webSocketRoutes) {
            Map<String, String> pathParams = matchWebSocketPath(entry.path, stubRequest.path);
            if (pathParams != null) {
                return new StubWebSocketClient(entry.handler, pathParams, stubRequest.queryParams);
            }
        }
        throw new IllegalArgumentException("No WebSocket route registered for path: " + stubRequest.path);
    }

    private Map<String, String> matchWebSocketPath(String pattern, String actual) {
        String[] patternParts = pattern.split("/");
        String[] actualParts = actual.split("/");
        if (patternParts.length != actualParts.length) {
            return null;
        }
        Map<String, String> pathParams = new LinkedHashMap<>();
        for (int i = 0; i < patternParts.length; i++) {
            if (patternParts[i].startsWith(":")) {
                pathParams.put(patternParts[i].substring(1), actualParts[i]);
            } else if (!patternParts[i].equals(actualParts[i])) {
                return null;
            }
        }
        return pathParams;
    }

    private static class WebSocketRouteEntry {
        final String path;
        final WebSocketRouteHandler<?, ?, ?> handler;

        WebSocketRouteEntry(String path, WebSocketRouteHandler<?, ?, ?> handler) {
            this.path = path;
            this.handler = handler;
        }
    }

    private void addCorsResponseHeaders(StubRequest stubRequest, StubHttpResponse response) {
        String origin = stubRequest.headers.get("Origin");
        if (origin == null || !corsConfig.isOriginAllowed(origin)) {
            return;
        }

        String allowOrigin = corsConfig.getAllowedOrigins().contains("*") ? "*" : origin;
        response.withHeader("Access-Control-Allow-Origin", allowOrigin);

        if (corsConfig.isAllowCredentials()) {
            response.withHeader("Access-Control-Allow-Credentials", "true");
        }

        if (!corsConfig.getExposedHeaders().isEmpty()) {
            response.withHeader("Access-Control-Expose-Headers",
                String.join(",", corsConfig.getExposedHeaders()));
        }
    }
}
