package io.kiw.web.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

public class WebSocketRouteHandler<IN, OUT, APP> {

    private final WebSocketRoute<IN, OUT, APP> route;
    private final ObjectMapper objectMapper;
    private final APP appState;
    private final Consumer<Exception> exceptionHandler;

    public WebSocketRouteHandler(WebSocketRoute<IN, OUT, APP> route, ObjectMapper objectMapper, APP appState, Consumer<Exception> exceptionHandler) {
        this.route = route;
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
    }

    public WebSocketSession<?> createSession(WebSocketConnection connection) {
        return new WebSocketSession<>(connection, objectMapper);
    }

    @SuppressWarnings("unchecked")
    public void onOpen(WebSocketSession<?> session) {
        try {
            route.onOpen((WebSocketSession<OUT>) session, appState);
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void onMessage(String rawMessage, WebSocketSession<?> session) {
        try {
            IN message = objectMapper.readValue(rawMessage, route);
            route.onMessage(message, (WebSocketSession<OUT>) session, appState);
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void onClose(WebSocketSession<?> session) {
        try {
            route.onClose((WebSocketSession<OUT>) session, appState);
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }
}
