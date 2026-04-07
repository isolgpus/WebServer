package io.kiw.luxis.web.internal;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.pipeline.DisconnectSession;
import io.kiw.luxis.web.pipeline.JustSendValidationError;
import io.kiw.luxis.web.pipeline.SendErrorResponse;
import io.kiw.luxis.web.pipeline.SendValidationErrorsAndDisconnectSession;
import io.kiw.luxis.web.websocket.WebSocketResponseMessage;
import io.kiw.luxis.web.websocket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WebSocketPipelineExecutor {

    enum ThreadContext { APPLICATION_CONTEXT, BLOCKING }

    private final ObjectMapper objectMapper;
    private final Object appState;
    private final Consumer<Exception> exceptionHandler;
    private final ExecutionDispatcher executionDispatcher;
    private final WebSocketRouteConfig config;
    private final Map<Class<?>, String> responseTypeRegistry;
    private final PendingAsyncResponses pendingAsyncResponses;

    public WebSocketPipelineExecutor(final ObjectMapper objectMapper, final Object appState, final Consumer<Exception> exceptionHandler, final ExecutionDispatcher executionDispatcher, final WebSocketRouteConfig config, final Map<Class<?>, String> responseTypeRegistry, final PendingAsyncResponses pendingAsyncResponses) {
        this.objectMapper = objectMapper;
        this.appState = appState;
        this.exceptionHandler = exceptionHandler;
        this.executionDispatcher = executionDispatcher;
        this.config = config;
        this.responseTypeRegistry = responseTypeRegistry;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    @SuppressWarnings("unchecked")
    public void execute(final WebSocketSession<?> session, final WebSocketPipeline<?> pipeline, final Object message) {
        final LuxisMapInstruction<ErrorMessageResponse> instruction = pipeline.getApplicationInstructions().getFirst();
        executeInstruction(session, pipeline, instruction, message, ThreadContext.APPLICATION_CONTEXT);
    }

    public void handleCorruptInput(final WebSocketSession<?> session) {
        switch (config.corruptInputStrategy()) {
            case DisconnectSession ignored -> session.close();
            case SendErrorResponse sendErrorResponse -> session.sendRaw(sendErrorResponse.message());
        }
    }

    private void executeInstruction(final WebSocketSession<?> session, final WebSocketPipeline<?> pipeline, final LuxisMapInstruction<ErrorMessageResponse> instruction, final Object message, final ThreadContext currentThread) {
        final ThreadContext requiredThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;

        runOnThread(requiredThread, currentThread, () -> {
            handleAndContinue(session, pipeline, instruction, message);
        });
    }

    @SuppressWarnings("unchecked")
    private void handleAndContinue(final WebSocketSession<?> session, final WebSocketPipeline<?> pipeline, final LuxisMapInstruction<ErrorMessageResponse> instruction, final Object message) {
        if (instruction.isAsync) {
            final CompletableFuture<Result<ErrorMessageResponse, Object>> future;
            try {
                future = instruction.handleAsync(message, session, appState, pendingAsyncResponses);
            } catch (final Exception e) {
                exceptionHandler.accept(e);
                return;
            }

            executionDispatcher.handleOnApplicationContext(future, exceptionHandler, (r) -> {
                try {
                    r.consume(e -> {
                        handleFailure(session, instruction, e);
                    }, q -> {
                        continueChain(session, pipeline, instruction, q, ThreadContext.APPLICATION_CONTEXT);
                    });
                } catch (final Exception e) {
                    exceptionHandler.accept(e);
                }
            });

        } else {
            final ThreadContext afterThread = instruction.isBlocking ? ThreadContext.BLOCKING : ThreadContext.APPLICATION_CONTEXT;
            final Result<ErrorMessageResponse, Object> result;
            try {
                result = instruction.handle(message, session, appState);
            } catch (final Exception e) {
                exceptionHandler.accept(e);
                return;
            }
            result.consume(e -> {
                runOnThread(ThreadContext.APPLICATION_CONTEXT, afterThread, () -> {
                    handleFailure(session, instruction, e);
                });
            }, q -> {
                continueChain(session, pipeline, instruction, q, afterThread);
            });
        }
    }

    private void handleFailure(final WebSocketSession<?> session, final LuxisMapInstruction<ErrorMessageResponse> instruction, final ErrorMessageResponse error) {
        if (instruction.isValidation()) {
            switch (config.failedValidationStrategy()) {
                case JustSendValidationError ignored -> sendErrorResponse(session, error);
                case SendValidationErrorsAndDisconnectSession ignored -> {
                    sendErrorResponse(session, error).thenAccept(result -> {
                        session.close();
                    });
                }
                case DisconnectSession ignored -> session.close();
            }
        } else {
            sendErrorResponse(session, error);
        }
    }

    private void continueChain(final WebSocketSession<?> session, final WebSocketPipeline<?> pipeline, final LuxisMapInstruction<ErrorMessageResponse> instruction, final Object result, final ThreadContext currentThread) {
        if (instruction.next().isPresent()) {
            final LuxisMapInstruction<ErrorMessageResponse> next = instruction.next().get();
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

    private void sendFinalResponse(final WebSocketSession<?> session, final Object result) {
        final String typeKey = responseTypeRegistry.get(result.getClass());
        if (typeKey == null) {
            throw new IllegalArgumentException("Unregistered response type: " + result.getClass().getName());
        }
        final WebSocketResponseMessage envelope = new WebSocketResponseMessage(typeKey, result);
        session.sendRaw(objectMapper.writeValueAsString(envelope));
    }

    private CompletableFuture<Void> sendErrorResponse(final WebSocketSession<?> session, final ErrorMessageResponse error) {
        final WebSocketResponseMessage envelope = new WebSocketResponseMessage("error", error);
        return session.sendRaw(objectMapper.writeValueAsString(envelope));
    }
}
