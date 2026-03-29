package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.DisconnectSession;
import io.kiw.luxis.web.pipeline.JustSendValidationError;
import io.kiw.luxis.web.pipeline.SendErrorResponse;
import io.kiw.luxis.web.pipeline.SendValidationErrorsAndDisconnectSession;
import io.kiw.luxis.web.websocket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketPipelineExecutor {

    enum ThreadContext { APPLICATION_CONTEXT, BLOCKING }

    private final ObjectMapper objectMapper;
    private final Object appState;
    private final Consumer<Exception> exceptionHandler;
    private final ExecutionDispatcher executionDispatcher;
    private final WebSocketRouteConfig config;

    public WebSocketPipelineExecutor(final ObjectMapper objectMapper, final Object appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final WebSocketRouteConfig config) {
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.executionDispatcher = executionDispatcher;
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public void execute(final WebSocketSession session, final IndividualMessageWebSocketPipeline<?> pipeline, final Object message) {
        final WebSocketMapInstruction webSocketMapInstruction = pipeline.getApplicationInstructions().getFirst();
        executeInstruction(session, pipeline, webSocketMapInstruction, message, ThreadContext.APPLICATION_CONTEXT);
    }

    public void handleCorruptInput(final WebSocketSession session) {
        switch (config.corruptInputStrategy()) {
            case DisconnectSession ignored -> session.close();
            case SendErrorResponse sendErrorResponse -> session.connection().sendText(sendErrorResponse.message());
        }
    }

    @SuppressWarnings("unchecked")
    private <IN, OUT, APP> void executeInstruction(final WebSocketSession session, final IndividualMessageWebSocketPipeline<?> pipeline, final WebSocketMapInstruction<IN, OUT, APP> instruction, final IN message, final ThreadContext currentThread) {
        final ThreadContext requiredThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;

        runOnThread(requiredThread, currentThread, () -> {
            handleAndContinue(session, pipeline, instruction, message);
        });
    }

    @SuppressWarnings({"unchecked", "checkstyle:FinalLocalVariable"})
    private <IN, OUT, APP> void handleAndContinue(final WebSocketSession session, final IndividualMessageWebSocketPipeline<?> pipeline, final WebSocketMapInstruction<IN, OUT, APP> instruction, final IN message) {
        if (instruction.isAsync) {
            final CompletableFuture<Result<ErrorMessageResponse, OUT>> future;
            try {
                future = instruction.handleAsync(message, session.connection(), (APP) appState);
            } catch (final Exception e) {
                exceptionHandler.accept(e);
                return;
            }

            executionDispatcher.handleOnApplicationContext(future, exceptionHandler, (r) -> {
                try {
                    r.consume(e -> {
                        handleFailure(session, instruction, e);
                    }, q -> {
                        continueChain(session, pipeline, instruction, (OUT) q, ThreadContext.APPLICATION_CONTEXT);
                    });
                } catch (final Exception e) {
                    exceptionHandler.accept(e);
                }
            });

        } else {
            final ThreadContext afterThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;
            final Result<ErrorMessageResponse, ?> result;
            try {
                result = instruction.handle(message, session.connection(), (APP) appState);
            } catch (final Exception e) {
                exceptionHandler.accept(e);
                return;
            }
            result.consume(e -> {
                runOnThread(ThreadContext.APPLICATION_CONTEXT, afterThread, () -> {
                    handleFailure(session, instruction, e);
                });
            }, q -> {
                continueChain(session, pipeline, instruction, (OUT) q, afterThread);
            });
        }
    }

    private void handleFailure(final WebSocketSession session, final WebSocketMapInstruction<?, ?, ?> instruction, final ErrorMessageResponse error) {
        if (instruction.isValidation()) {
            switch (config.failedValidationStrategy()) {
                case JustSendValidationError ignored -> sendFinalResponse(session, error);
                case SendValidationErrorsAndDisconnectSession ignored -> {
                    sendFinalResponse(session, error).thenAccept(result -> {
                        session.close();
                    });
                }
                case DisconnectSession ignored -> session.close();
            }
        } else {
            sendFinalResponse(session, error);
        }
    }

    @SuppressWarnings("unchecked")
    private <OUT> void continueChain(final WebSocketSession session, final IndividualMessageWebSocketPipeline<?> pipeline, final WebSocketMapInstruction<?, OUT, ?> instruction, final OUT result, final ThreadContext currentThread) {
        if (instruction.next().isPresent()) {
            final WebSocketMapInstruction next = instruction.next().get();
            executeInstruction(session, pipeline, next, result, currentThread);
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

    private <OUT> CompletableFuture<Void> sendFinalResponse(final WebSocketSession session, final OUT result) {
        return session.connection().sendText(objectMapper.writeValueAsString(result));
    }
}
