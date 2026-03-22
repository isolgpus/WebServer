package io.kiw.luxis.web.internal;

import tools.jackson.databind.ObjectMapper;
import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.pipeline.DisconnectSession;
import io.kiw.luxis.web.pipeline.ErrorResponse;
import io.kiw.luxis.web.pipeline.WebSocketStream;
import io.kiw.luxis.web.websocket.WebSocketConnection;
import io.kiw.luxis.web.websocket.WebSocketSession;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketRouteHandler<IN, OUT, APP> {

    enum ThreadContext { APPLICATION_CONTEXT, BLOCKING }

    private final WebSocketRoute<IN, OUT, APP> route;
    private final ObjectMapper objectMapper;
    private final APP appState;
    private final Consumer<Exception> exceptionHandler;
    private final ExecutionDispatcher executionDispatcher;
    private final WebSocketPipeline<OUT> pipeline;
    private final WebSocketRouteConfig config;

    public WebSocketRouteHandler(final WebSocketRoute<IN, OUT, APP> route, final ObjectMapper objectMapper, final APP appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final WebSocketRouteConfig config) {
        this.route = route;
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.executionDispatcher = executionDispatcher;
        this.pipeline = route.onMessage(new WebSocketStream<>(new ArrayList<>(), appState));
        this.config = config;
    }

    public WebSocketSession<?> createSession(final WebSocketConnection connection) {
        return new WebSocketSession<>(connection, objectMapper);
    }

    @SuppressWarnings("unchecked")
    public void onOpen(final WebSocketSession<?> session) {
        try {
            route.onOpen((WebSocketSession<OUT>) session, appState);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void onMessage(final String rawMessage, final WebSocketSession<?> session) {
        try {
            final IN message = objectMapper.readValue(rawMessage, route);

            final WebSocketMapInstruction webSocketMapInstruction = pipeline.getApplicationInstructions().getFirst();

            executeInstruction(session, webSocketMapInstruction, message, ThreadContext.APPLICATION_CONTEXT);

        } catch (final Exception e) {
            handleCorruptInput(e, session);
        }
    }

    private void handleCorruptInput(final Exception e, final WebSocketSession<?> session) {
        switch (config.corruptInputStrategy()) {
            case DisconnectSession ignored -> session.close();
            case ErrorResponse errorResponse -> session.connection().sendText(errorResponse.message());
        }
    }

    private <IN, OUT> void executeInstruction(final WebSocketSession<?> session, final WebSocketMapInstruction<IN, OUT, APP> instruction, final IN message, final ThreadContext currentThread) {
        final ThreadContext requiredThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;

        runOnThread(requiredThread, currentThread, () -> {
            handleAndContinue(session, instruction, message);
        });
    }

    @SuppressWarnings("unchecked")
    private <IN, OUT> void handleAndContinue(final WebSocketSession<?> session, final WebSocketMapInstruction<IN, OUT, APP> instruction, final IN message) {
        if (instruction.isAsync) {
            final CompletableFuture<Result<ErrorMessageResponse, OUT>> future = instruction.handleAsync(message, session.connection(), appState);

            executionDispatcher.handleOnApplicationContext(() -> {
                try {
                    future.join().consume(e -> {
                        sendFinalResponse(session, e);
                    }, q -> {
                        continueChain(session, instruction, (OUT) q, ThreadContext.APPLICATION_CONTEXT);
                    });
                } catch (final Exception e) {
                    exceptionHandler.accept(e);
                }
            });
        } else {
            final ThreadContext afterThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;
            final Result<ErrorMessageResponse, ?> result = instruction.handle(message, session.connection(), appState);
            result.consume(e -> {
                runOnThread(ThreadContext.APPLICATION_CONTEXT, afterThread, () -> {
                    sendFinalResponse(session, e);
                });
            }, q -> {
                continueChain(session, instruction, (OUT) q, afterThread);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <OUT> void continueChain(final WebSocketSession<?> session, final WebSocketMapInstruction<?, OUT, APP> instruction, final OUT result, final ThreadContext currentThread) {
        if (instruction.next().isPresent()) {
            final WebSocketMapInstruction<OUT, ?, APP> next = (WebSocketMapInstruction<OUT, ?, APP>) instruction.next().get();
            executeInstruction(session, next, result, currentThread);
        } else if (pipeline.shouldSendResponse()) {
            runOnThread(ThreadContext.APPLICATION_CONTEXT, currentThread, () -> {
                sendFinalResponse(session, result);
            });
        }
    }

    private void runOnThread(final ThreadContext required, final ThreadContext current, final Runnable action) {
        if (current == required) {
            action.run();
        } else if (required == ThreadContext.BLOCKING) {
            executionDispatcher.handleBlocking(action);
        } else {
            executionDispatcher.handleOnApplicationContext(action);
        }
    }

    private <OUT> void sendFinalResponse(final WebSocketSession<?> session, final OUT result) {
        session.connection().sendText(objectMapper.writeValueAsString(result));
    }

    @SuppressWarnings("unchecked")
    public void onClose(final WebSocketSession<?> session) {
        try {
            route.onClose((WebSocketSession<OUT>) session, appState);
        } catch (final Exception e) {
            exceptionHandler.accept(e);
        }
    }
}
