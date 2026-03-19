package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kiw.result.Result;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketRouteHandler<IN, OUT, APP> {

    enum ThreadContext { EVENT_LOOP, BLOCKING }

    private final WebSocketRoute<IN, OUT, APP> route;
    private final ObjectMapper objectMapper;
    private final APP appState;
    private final Consumer<Exception> exceptionHandler;
    private final WebSocketRouterWrapper webSocketRouterWrapper;
    private final WebSocketPipeline<OUT> pipeline;

    public WebSocketRouteHandler(WebSocketRoute<IN, OUT, APP> route, ObjectMapper objectMapper, APP appState, Consumer<Exception> exceptionHandler, WebSocketRouterWrapper webSocketRouterWrapper) {
        this.route = route;
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.webSocketRouterWrapper = webSocketRouterWrapper;
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

            WebSocketMapInstruction webSocketMapInstruction = pipeline.getApplicationInstructions().getFirst();

            executeInstruction(session, webSocketMapInstruction, message, ThreadContext.EVENT_LOOP);

        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    private <IN, OUT> void executeInstruction(WebSocketSession<?> session, WebSocketMapInstruction<IN, OUT, APP> instruction, IN message, ThreadContext currentThread) {
        ThreadContext requiredThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.EVENT_LOOP;

        runOnThread(requiredThread, currentThread, () -> {
            handleAndContinue(session, instruction, message);
        });
    }

    @SuppressWarnings("unchecked")
    private <IN, OUT> void handleAndContinue(WebSocketSession<?> session, WebSocketMapInstruction<IN, OUT, APP> instruction, IN message) {
        if(instruction.isAsync)
        {
            CompletableFuture<Result<ErrorMessageResponse, OUT>> future = instruction.handleAsync(message, session.connection(), appState);

            webSocketRouterWrapper.handleOnEventLoop(() -> {
                try
                {
                    future.join().consume(e -> {}, q -> {
                        continueChain(session, instruction, (OUT) q, ThreadContext.EVENT_LOOP);
                    });
                }
                catch (Exception e)
                {
                    exceptionHandler.accept(e);
                }
            });
        }
        else
        {
            ThreadContext afterThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.EVENT_LOOP;
            Result<ErrorMessageResponse, ?> result = instruction.handle(message, session.connection(), appState);
            result.consume(e -> {}, q -> {
                continueChain(session, instruction, (OUT) q, afterThread);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <OUT> void continueChain(WebSocketSession<?> session, WebSocketMapInstruction<?, OUT, APP> instruction, OUT result, ThreadContext currentThread) {
        if(instruction.next().isPresent())
        {
            WebSocketMapInstruction<OUT, ?, APP> next = (WebSocketMapInstruction<OUT, ?, APP>) instruction.next().get();
            executeInstruction(session, next, result, currentThread);
        }
        else
        {
            runOnThread(ThreadContext.EVENT_LOOP, currentThread, () -> {
                sendFinalResponse(session, result);
            });
        }
    }

    private void runOnThread(ThreadContext required, ThreadContext current, Runnable action) {
        if(current == required)
        {
            action.run();
        }
        else if(required == ThreadContext.BLOCKING)
        {
            webSocketRouterWrapper.handleBlocking(action);
        }
        else
        {
            webSocketRouterWrapper.handleOnEventLoop(action);
        }
    }

    private <OUT> void sendFinalResponse(WebSocketSession<?> session, OUT result) {
        try {
            session.connection().sendText(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
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
