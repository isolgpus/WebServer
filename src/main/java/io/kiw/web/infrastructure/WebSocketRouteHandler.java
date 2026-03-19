package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kiw.result.Result;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WebSocketRouteHandler<IN, OUT, APP> {

    private final WebSocketRoute<IN, OUT, APP> route;
    private final ObjectMapper objectMapper;
    private final APP appState;
    private final Consumer<Exception> exceptionHandler;
    private final WebSocketPipeline<OUT> pipeline;

    public WebSocketRouteHandler(WebSocketRoute<IN, OUT, APP> route, ObjectMapper objectMapper, APP appState, Consumer<Exception> exceptionHandler) {
        this.route = route;
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.pipeline = route.onMessage(new WebSocketStream<>(new ArrayList<>(), appState));
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
            WebSocketConnection connection = session.connection();
            Object state = message;

            for (Object raw : pipeline.getApplicationInstructions()) {
                WebSocketMapInstruction<Object, Object, Object> instruction =
                    (WebSocketMapInstruction<Object, Object, Object>) raw;

                Result<ErrorMessageResponse, Object> result =
                    instruction.handle(state, connection, pipeline.getApplicationState());

                Object[] holder = new Object[1];
                boolean[] isError = {false};

                result.consume(
                    error -> {
                        isError[0] = true;
                        try {
                            connection.sendText(objectMapper.writeValueAsString(error));
                        } catch (JsonProcessingException e) {
                            exceptionHandler.accept(e);
                        }
                    },
                    success -> holder[0] = success
                );

                if (isError[0]) {
                    return;
                }

                if (instruction.lastStep) {
                    if (holder[0] != null) {
                        connection.sendText(objectMapper.writeValueAsString(holder[0]));
                    }
                    return;
                }

                state = holder[0];
            }
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
