package io.kiw.web.infrastructure;

import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.test.handler.RouteConfig;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

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
    public void configureCors(CorsConfig corsConfig) {
        CorsHandler corsHandler = CorsHandler.create();
        for (String origin : corsConfig.getAllowedOrigins()) {
            corsHandler.addOrigin(origin);
        }
        for (String method : corsConfig.getAllowedMethods()) {
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
    public void route(String path, Method method, String consumes, String produces, RequestPipeline flow, RouteConfig routeConfig) {
        Route route = router.route(method.getVertxMethod(), path).consumes(consumes).produces(produces);
        registerHandlers(route, flow, routeConfig);
    }

    @Override
    public void route(String path, String consumes, String produces, RequestPipeline flow, RouteConfig routeConfig) {
        Route route = router.route(path).consumes(consumes).produces(produces);
        registerHandlers(route, flow, routeConfig);
    }

    @Override
    protected void webSocketRoute(String path, WebSocketRouteHandler<?, ?, ?> handler) {
        router.route(path).handler(ctx -> {
            ctx.request().toWebSocket()
                .onSuccess(ws -> {
                    VertxWebSocketConnection connection = new VertxWebSocketConnection(ws, ctx);
                    WebSocketSession<?> session = handler.createSession(connection);
                    handler.onOpen(session);
                    ws.textMessageHandler(msg -> handler.onMessage(msg, session));
                    ws.closeHandler(v -> handler.onClose(session));
                })
                .onFailure(err -> {
                    ctx.response().setStatusCode(500).end();
                });
        });
    }

    private void registerHandlers(Route route, RequestPipeline flow, RouteConfig routeConfig) {
        int timeout = routeConfig.timeoutInMillis.orElse(defaultTimeoutMillis);

        route.handler(new VertxTimeoutHandler(timeout));

        for (Object what : flow.getApplicationInstructions()) {
            MapInstruction applicationInstruction = (MapInstruction) what;
            if (applicationInstruction.isAsync && applicationInstruction.isBlocking) {
                route.blockingHandler(ctx -> handleAsyncBlocking(applicationInstruction, new VertxContextImpl(ctx), flow.getApplicationState(), flow.getEnder()));
            } else if (applicationInstruction.isAsync) {
                route.handler(ctx -> handleAsync(applicationInstruction, new VertxContextImpl(ctx), flow.getApplicationState(), flow.getEnder()));
            } else if (applicationInstruction.isBlocking) {
                route.blockingHandler(ctx -> handle(applicationInstruction, new VertxContextImpl(ctx), null, flow.getEnder()));
            } else {
                route.handler(ctx -> handle(applicationInstruction, new VertxContextImpl(ctx), flow.getApplicationState(), flow.getEnder()));
            }
        }
    }

}
