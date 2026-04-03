package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.handler.WebSocketRoutes;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.kiw.luxis.web.websocket.WebSocketMessage;
import io.kiw.luxis.web.websocket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HttpWebSocketRouteHandlerImpl<APP, RESP> implements HttpWebSocketRouteHandler {

    private final WebSocketRoutes<APP, RESP> route;
    private final ObjectMapper objectMapper;
    private final APP appState;
    private final Consumer<Exception> exceptionHandler;
    private final WebSocketPipelineExecutor executor;
    private final WebSocketRoutesRegister<APP, RESP> routesRegister;
    private final LinkedHashMap<String, WebSocketRoute<?>> routes;
    private final Map<Class<?>, String> responseTypeRegistry;

    public HttpWebSocketRouteHandlerImpl(final WebSocketRoutes<APP, RESP> route, final ObjectMapper objectMapper, final APP appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final WebSocketRouteConfig config, final PendingAsyncResponses pendingAsyncResponses) {
        this.route = route;
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.responseTypeRegistry = new HashMap<>();
        routes = new LinkedHashMap<>();
        routesRegister = new WebSocketRoutesRegister<>(appState, pendingAsyncResponses, routes, responseTypeRegistry);
        route.registerRoutes(routesRegister);
        this.executor = new WebSocketPipelineExecutor(objectMapper, appState, exceptionHandler, executionDispatcher, config, responseTypeRegistry, pendingAsyncResponses);
    }

    @Override
    public WebSocketSession<?> createSession(final WebSocketConnection connection) {
        return new WebSocketSession<>(connection, objectMapper, responseTypeRegistry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onOpen(final WebSocketSession<?> session) {
        try {
            route.onOpen((WebSocketSession<RESP>) session, appState);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(final String rawMessage, final WebSocketSession<?> session) {
        try {
            final WebSocketMessage envelope = objectMapper.readValue(rawMessage, WebSocketMessage.class);

            if (envelope.type() == null) {
                executor.handleCorruptInput(session);
                return;
            }


            final WebSocketRoute<?> branch = routes.get(envelope.type());
            if (branch == null) {
                executor.handleCorruptInput(session);
                return;
            }

            final Object payload = objectMapper.treeToValue(envelope.payload(), branch.messageType());
            executor.execute(session, branch.pipeline(), payload);

        } catch (final Exception e) {
            executor.handleCorruptInput(session);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClose(final WebSocketSession<?> session) {
        try {
            route.onClose((WebSocketSession<RESP>) session, appState);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
        }
    }
}
