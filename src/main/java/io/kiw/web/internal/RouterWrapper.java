package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kiw.web.cors.CorsConfig;
import io.kiw.web.internal.ender.Ender;
import io.kiw.web.test.handler.RouteConfig;
import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class RouterWrapper {

    private final Consumer<Exception> exceptionHandler;

    public RouterWrapper(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    Consumer<Exception> getExceptionHandler() {
        return exceptionHandler;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected abstract void route(String path, Method method, String consumes, String provides, RequestPipeline flow, RouteConfig routeConfig);

    protected abstract void route(String path, String consumes, String provides, RequestPipeline flow, RouteConfig routeConfig);

    public abstract void configureCors(CorsConfig corsConfig);

    protected abstract void webSocketRoute(String path, WebSocketRouteHandler<?, ?, ?> handler);

    public <T> void handle(MapInstruction<Object, T, Object> applicationInstruction, RequestContext vertxContext, Object applicationState, Ender ender) {
        HttpContext httpContext = new HttpContext(vertxContext);
        Result<HttpErrorResponse, T> result;
        try {
            result = applicationInstruction.handle(vertxContext.get("state"), httpContext, applicationState);
        } catch (Exception e) {
            handleException(vertxContext, e);
            return;
        }

        processResult(result, applicationInstruction, vertxContext, ender);
    }

     <T> void handleAsync(MapInstruction<Object, T, Object> applicationInstruction, RequestContext vertxContext, Object applicationState, Ender ender) {
        HttpContext httpContext = new HttpContext(vertxContext);
        CompletableFuture<Result<HttpErrorResponse, T>> future;
        try {
            future = applicationInstruction.handleAsync(vertxContext.get("state"), httpContext, applicationState);
        } catch (Exception e) {
            handleException(vertxContext, e);
            return;
        }

        // whenComplete fires on whichever thread completes the future (e.g. ForkJoinPool).
        // We must dispatch back to the Vert.x event loop before touching the pipeline
        // (ctx.next(), ctx.end(), etc. are not thread-safe outside the event loop).
        future.whenComplete((result, throwable) ->
            vertxContext.runOnContext(() -> {
                if (throwable != null) {
                    handleException(vertxContext, throwable instanceof Exception ? (Exception) throwable : new RuntimeException(throwable));
                    return;
                }
                processResult(result, applicationInstruction, vertxContext, ender);
            })
        );
    }

    public <T> void handleAsyncBlocking(MapInstruction<Object, T, Object> applicationInstruction, RequestContext vertxContext, Object applicationState, Ender ender) {
        HttpContext httpContext = new HttpContext(vertxContext);
        try {
            Result<HttpErrorResponse, T> result = applicationInstruction.handleAsync(vertxContext.get("state"), httpContext, applicationState).join();
            processResult(result, applicationInstruction, vertxContext, ender);
        } catch (Exception e) {
            handleException(vertxContext, e);
        }
    }

    private <T> void processResult(Result<HttpErrorResponse, T> result, MapInstruction<Object, T, Object> applicationInstruction, RequestContext vertxContext, Ender ender) {
        result.consume(httpErrorResponse -> {
                try {
                    vertxContext.setStatusCode(httpErrorResponse.statusCode);
                    vertxContext.end(this.objectMapper.writeValueAsString(httpErrorResponse.errorMessageValue));
                } catch (JsonProcessingException e) {
                    handleException(vertxContext, e);
                }
            },
            s -> {
                if (applicationInstruction.lastStep) {
                    try {
                        Object value = s;
                        if (s instanceof HttpSuccessResponse<?> successResponse) {
                            vertxContext.setStatusCode(successResponse.statusCode);
                            value = successResponse.value;
                        }
                        ender.end(vertxContext, value);
                    } catch (RuntimeException e) {
                        handleException(vertxContext, e);
                    }
                } else {
                    vertxContext.put("state", s);
                    vertxContext.next();
                }
            });
    }

    private void handleException(RequestContext vertxContext, Exception e) {
        exceptionHandler.accept(e);
        vertxContext.setStatusCode(500);
        vertxContext.end("{\"message\":\"Something went wrong\"}");
    }
}
