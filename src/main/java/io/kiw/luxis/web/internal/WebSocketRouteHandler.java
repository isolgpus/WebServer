package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.pipeline.WebSocketSplitStream;
import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.kiw.luxis.web.websocket.WebSocketMessage;
import io.kiw.luxis.web.websocket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

public class WebSocketRouteHandler<SPLIT, APP> implements WebSocketHandler {

    private final WebSocketRoute<SPLIT, APP> route;
    private final ObjectMapper objectMapper;
    private final APP appState;
    private final Consumer<Exception> exceptionHandler;
    private final WebSocketPipeline splitPipeline;
    private final WebSocketPipelineExecutor executor;

    public WebSocketRouteHandler(final WebSocketRoute<SPLIT, APP> route, final ObjectMapper objectMapper, final APP appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final WebSocketRouteConfig config, final PendingAsyncResponses pendingAsyncResponses) {
        this.route = route;
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.splitPipeline = route.onMessage(new WebSocketSplitStream<>(appState, pendingAsyncResponses));
        this.executor = new WebSocketPipelineExecutor(objectMapper, appState, exceptionHandler, executionDispatcher, config);
    }

    @Override
    public WebSocketSession createSession(final WebSocketConnection connection) {
        return new WebSocketSession(connection, objectMapper);
    }

    @Override
    public void onOpen(final WebSocketSession session) {
        try {
            route.onOpen(session, appState);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(final String rawMessage, final WebSocketSession session) {
        try {
            final WebSocketMessage envelope = objectMapper.readValue(rawMessage, WebSocketMessage.class);

            if (envelope.type() == null) {
                executor.handleCorruptInput(session);
                return;
            }

            final SplitBranch<?> branch = splitPipeline.getBranch(envelope.type());
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
    public void onClose(final WebSocketSession session) {
        try {
            route.onClose(session, appState);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
        }
    }
}
