package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.pipeline.BackpressureStrategy;
import io.kiw.luxis.web.pipeline.DisconnectSession;
import io.kiw.luxis.web.pipeline.JustSendValidationError;
import io.kiw.luxis.web.pipeline.WebSocketRoutesRegister;
import io.kiw.luxis.web.websocket.ClientWebSocketRoutes;
import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.kiw.luxis.web.websocket.WebSocketMessage;
import io.kiw.luxis.web.websocket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClientWebSocketHandler<APP, RESP> {

    private final ObjectMapper objectMapper;
    private final WebSocketPipelineExecutor executor;
    private final LinkedHashMap<String, WebSocketRoute<?>> routes;
    private final Map<Class<?>, String> responseTypeRegistry;

    public ClientWebSocketHandler(final ClientWebSocketRoutes<APP, RESP> clientRoutes, final ObjectMapper objectMapper, final ExecutionDispatcher executionDispatcher, final PendingAsyncResponses pendingAsyncResponses, final Consumer<Exception> exceptionHandler) {
        this.objectMapper = objectMapper;
        this.responseTypeRegistry = new HashMap<>();
        this.routes = new LinkedHashMap<>();
        final WebSocketRoutesRegister<APP, RESP> routesRegister = new WebSocketRoutesRegister<>(null, pendingAsyncResponses, routes, responseTypeRegistry);
        clientRoutes.registerRoutes(routesRegister);
        final WebSocketRouteConfig config = new WebSocketRouteConfig(DisconnectSession.INSTANCE, JustSendValidationError.INSTANCE, BackpressureStrategy.UNBOUNDED_BUFFER);
        this.executor = new WebSocketPipelineExecutor(objectMapper, null, exceptionHandler, executionDispatcher, config, responseTypeRegistry, pendingAsyncResponses);
    }

    public WebSocketSession<RESP> createSession(final WebSocketConnection connection) {
        return new WebSocketSession<>(connection, objectMapper, responseTypeRegistry, BackpressureStrategy.UNBOUNDED_BUFFER);
    }

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
}
