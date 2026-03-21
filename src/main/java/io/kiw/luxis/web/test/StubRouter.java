package io.kiw.luxis.web.test;

import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.internal.MapInstruction;
import io.kiw.luxis.web.internal.RequestPipeline;
import io.kiw.luxis.web.internal.RouterWrapper;
import io.kiw.luxis.web.internal.WebSocketRouteHandler;
import io.kiw.luxis.web.test.handler.RouteConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StubRouter extends RouterWrapper {
    private PathMatcher routes = new PathMatcher();
    private CorsConfig corsConfig;
    private final List<WebSocketRouteEntry> webSocketRoutes = new ArrayList<>();

    public StubRouter(final Consumer<Exception> exceptionHandler) {
        super(exceptionHandler);
    }

    @Override
    public void configureCors(final CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    @Override
    public void route(final String path, final Method method, final String consumes, final String provides, final RequestPipeline flow, final RouteConfig routeConfig) {
        routes.putRoute(path, method, flow);
    }

    @Override
    public void route(final String path, final String consumes, final String provides, final RequestPipeline flow, final RouteConfig routeConfig) {
        routes.putAllMethodRoute(path, flow);
    }

    public TestHttpResponse handle(final StubRequest stubRequest, final Method method) {
        if (corsConfig != null) {
            final String origin = stubRequest.headers.get("Origin");
            if (method == Method.OPTIONS) {
                if (origin != null) {
                    return handlePreflightRequest(stubRequest);
                }
                return new TestHttpResponse(null).withStatusCode(405);
            }
            if (origin != null) {
                boolean result = false;
                if (origin != null) {
                    result = corsConfig.getAllowedOrigins().contains("*") || corsConfig.getAllowedOrigins().contains(origin);
                }
                if (!result) {
                    return new TestHttpResponse(null).withStatusCode(403);
                }
            }
        }

        final StubRequestContext context = new StubRequestContext(stubRequest.body, stubRequest.queryParams, stubRequest.headers, stubRequest.cookies, stubRequest.fileUploads);
        final PathMatcher.MatchResult matchResult = this.routes.get(stubRequest.path, method);
        context.setPathParams(matchResult.getPathParams());

        for (final RequestPipeline flow : matchResult.getFlows()) {
            for (final Object what : flow.getApplicationInstructions()) {
                final MapInstruction applicationInstruction = (MapInstruction) what;
                if (applicationInstruction.isAsync) {
                    this.handleAsyncBlocking(applicationInstruction, context, flow.getApplicationState(), flow.getEnder());
                } else {
                    this.handle(applicationInstruction, context, flow.getApplicationState(), flow.getEnder());
                }
                if (context.hasFinished()) {
                    break;
                }
            }
            if (context.hasFinished()) {
                break;
            }
        }

        final TestHttpResponse response = context.getResponse();
        if (corsConfig != null) {
            addCorsResponseHeaders(stubRequest, response);
        }
        return response;
    }

    private TestHttpResponse handlePreflightRequest(final StubRequest stubRequest) {
        final String origin = stubRequest.headers.get("Origin");
        boolean result = false;
        if (origin != null) {
            result = corsConfig.getAllowedOrigins().contains("*") || corsConfig.getAllowedOrigins().contains(origin);
        }
        if (origin == null || !result) {
            return new TestHttpResponse(null).withStatusCode(403);
        }

        final String allowOrigin = corsConfig.getAllowedOrigins().contains("*") ? "*" : origin;

        final TestHttpResponse response = new TestHttpResponse(null).withStatusCode(204)
            .withHeader("access-control-allow-origin", allowOrigin);

        if (!corsConfig.getAllowedMethods().isEmpty()) {
            response.withHeader("access-control-allow-methods",
                String.join(",", corsConfig.getAllowedMethods()));
        }

        if (!corsConfig.getAllowedHeaders().isEmpty()) {
            response.withHeader("access-control-allow-headers",
                String.join(",", corsConfig.getAllowedHeaders()));
        }

        if (corsConfig.isAllowCredentials()) {
            response.withHeader("access-control-allow-credentials", "true");
        }

        if (corsConfig.getMaxAgeSeconds() >= 0) {
            response.withHeader("access-control-max-age", String.valueOf(corsConfig.getMaxAgeSeconds()));
        }

        return response;
    }

    @Override
    protected void webSocketRoute(final String path, final WebSocketRouteHandler<?, ?, ?> handler) {
        webSocketRoutes.add(new WebSocketRouteEntry(path, handler));
    }

    public StubTestWebSocketClient webSocket(final StubRequest stubRequest) {
        for (final WebSocketRouteEntry entry : webSocketRoutes) {
            final Map<String, String> pathParams = matchWebSocketPath(entry.path, stubRequest.path);
            if (pathParams != null) {
                return new StubTestWebSocketClient(entry.handler);
            }
        }
        throw new IllegalArgumentException("No WebSocket route registered for path: " + stubRequest.path);
    }

    private Map<String, String> matchWebSocketPath(final String pattern, final String actual) {
        final String[] patternParts = pattern.split("/");
        final String[] actualParts = actual.split("/");
        if (patternParts.length != actualParts.length) {
            return null;
        }
        final Map<String, String> pathParams = new LinkedHashMap<>();
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

        WebSocketRouteEntry(final String path, final WebSocketRouteHandler<?, ?, ?> handler) {
            this.path = path;
            this.handler = handler;
        }
    }

    private void addCorsResponseHeaders(final StubRequest stubRequest, final TestHttpResponse response) {
        final String origin = stubRequest.headers.get("Origin");
        boolean result = false;
        if (origin != null) {
            result = corsConfig.getAllowedOrigins().contains("*") || corsConfig.getAllowedOrigins().contains(origin);
        }
        if (origin == null || !result) {
            return;
        }

        final String allowOrigin = corsConfig.getAllowedOrigins().contains("*") ? "*" : origin;
        response.withHeader("access-control-allow-origin", allowOrigin);

        if (corsConfig.isAllowCredentials()) {
            response.withHeader("access-control-allow-credentials", "true");
        }

        if (!corsConfig.getExposedHeaders().isEmpty()) {
            response.withHeader("access-control-expose-headers",
                String.join(",", corsConfig.getExposedHeaders()));
        }
    }
}
