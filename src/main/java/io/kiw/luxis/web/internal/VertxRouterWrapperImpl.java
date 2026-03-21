package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.test.handler.RouteConfig;
import io.kiw.luxis.web.websocket.WebSocketSession;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.function.Consumer;

public class VertxRouterWrapperImpl extends RouterWrapper {
    private final Router router;
    private final int defaultTimeoutMillis;

    public VertxRouterWrapperImpl(final Router router, final int defaultTimeoutMillis, final Consumer<Exception> exceptionHandler) {
        super(exceptionHandler);
        this.router = router;
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

    @Override
    public void configureCors(final CorsConfig corsConfig) {
        final CorsHandler corsHandler = CorsHandler.create();
        for (final String origin : corsConfig.getAllowedOrigins()) {
            corsHandler.addOrigin(origin);
        }
        for (final String method : corsConfig.getAllowedMethods()) {
            corsHandler.allowedMethod(HttpMethod.valueOf(method));
        }
        if (!corsConfig.getAllowedHeaders().isEmpty()) {
            corsHandler.allowedHeaders(corsConfig.getAllowedHeaders());
        }
        if (!corsConfig.getExposedHeaders().isEmpty()) {
            corsHandler.exposedHeaders(corsConfig.getExposedHeaders());
        }
        corsHandler.allowCredentials(corsConfig.isAllowCredentials());
        if (corsConfig.getMaxAgeSeconds() >= 0) {
            corsHandler.maxAgeSeconds(corsConfig.getMaxAgeSeconds());
        }
        router.route().handler(corsHandler);
    }

    @Override
    public void route(final String path, final Method method, final String consumes, final String produces, final RequestPipeline flow, final RouteConfig routeConfig) {
        final Route route = router.route(HttpMethod.valueOf(method.name()), path).consumes(consumes).produces(produces);
        registerHandlers(route, flow, routeConfig);
    }

    @Override
    public void route(final String path, final String consumes, final String produces, final RequestPipeline flow, final RouteConfig routeConfig) {
        final Route route = router.route(path).consumes(consumes).produces(produces);
        registerHandlers(route, flow, routeConfig);
    }

    @Override
    protected void webSocketRoute(final String path, final WebSocketRouteHandler<?, ?, ?> handler) {
        router.route(path).handler(ctx -> {
            ctx.request().toWebSocket()
                .onSuccess(ws -> {
                    final VertxWebSocketConnection connection = new VertxWebSocketConnection(ws);
                    final WebSocketSession<?> session = handler.createSession(connection);
                    handler.onOpen(session);
                    ws.textMessageHandler(msg -> handler.onMessage(msg, session));
                    ws.closeHandler(v -> handler.onClose(session));
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end();
                });
        });
    }

    private void registerHandlers(final Route route, final RequestPipeline flow, final RouteConfig routeConfig) {
        final int timeout = routeConfig.timeoutInMillis.orElse(defaultTimeoutMillis);

        route.handler(new VertxTimeoutHandler(timeout));

        for (final Object what : flow.getApplicationInstructions()) {
            final MapInstruction applicationInstruction = (MapInstruction) what;
            if (applicationInstruction.isAsync && applicationInstruction.isBlocking) {
                route.blockingHandler(ctx -> handleAsyncBlocking(applicationInstruction, new VertxRequestContextImpl(ctx), flow.getApplicationState(), flow.getEnder()));
            } else if (applicationInstruction.isAsync) {
                route.handler(ctx -> handleAsync(applicationInstruction, new VertxRequestContextImpl(ctx), flow.getApplicationState(), flow.getEnder()));
            } else if (applicationInstruction.isBlocking) {
                route.blockingHandler(ctx -> handle(applicationInstruction, new VertxRequestContextImpl(ctx), null, flow.getEnder()));
            } else {
                route.handler(ctx -> handle(applicationInstruction, new VertxRequestContextImpl(ctx), flow.getApplicationState(), flow.getEnder()));
            }
        }
    }

}
