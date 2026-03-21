package io.kiw.luxis.web.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.http.RequestContext;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.http.HttpContext;
import io.kiw.luxis.web.http.HttpErrorResponse;
import io.kiw.luxis.web.http.HttpSuccessResponse;
import io.kiw.luxis.web.internal.ender.Ender;
import io.kiw.luxis.web.test.handler.RouteConfig;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class RouterWrapper {

    private final Consumer<Exception> exceptionHandler;

    public RouterWrapper(final Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    Consumer<Exception> getExceptionHandler() {
        return exceptionHandler;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected abstract void route(final String path, final Method method, final String consumes, final String provides, final RequestPipeline flow, final RouteConfig routeConfig);

    protected abstract void route(final String path, final String consumes, final String provides, final RequestPipeline flow, final RouteConfig routeConfig);

    public abstract void configureCors(final CorsConfig corsConfig);

    protected abstract void webSocketRoute(final String path, final WebSocketRouteHandler<?, ?, ?> handler);

    public <T> void handle(final MapInstruction<Object, T, Object> applicationInstruction, final RequestContext vertxContext, final Object applicationState, final Ender ender) {
        final HttpContext httpContext = new HttpContext(vertxContext);
        final Result<HttpErrorResponse, T> result;
        try {
            result = applicationInstruction.handle(vertxContext.get("state"), httpContext, applicationState);
        } catch (final Exception e) {
            handleException(vertxContext, e);
            return;
        }

        processResult(result, applicationInstruction, vertxContext, ender);
    }

    <T> void handleAsync(final MapInstruction<Object, T, Object> applicationInstruction, final RequestContext vertxContext, final Object applicationState, final Ender ender) {
        final HttpContext httpContext = new HttpContext(vertxContext);
        final CompletableFuture<Result<HttpErrorResponse, T>> future;
        try {
            future = applicationInstruction.handleAsync(vertxContext.get("state"), httpContext, applicationState);
        } catch (final Exception e) {
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

    public <T> void handleAsyncBlocking(final MapInstruction<Object, T, Object> applicationInstruction, final RequestContext vertxContext, final Object applicationState, final Ender ender) {
        final HttpContext httpContext = new HttpContext(vertxContext);
        try {
            final Result<HttpErrorResponse, T> result = applicationInstruction.handleAsync(vertxContext.get("state"), httpContext, applicationState).join();
            processResult(result, applicationInstruction, vertxContext, ender);
        } catch (final Exception e) {
            handleException(vertxContext, e);
        }
    }

    @SuppressWarnings("IllegalCatch")
    private <T> void processResult(final Result<HttpErrorResponse, T> result, final MapInstruction<Object, T, Object> applicationInstruction, final RequestContext vertxContext, final Ender ender) {
        result.consume(httpErrorResponse -> {
            try {
                vertxContext.setStatusCode(httpErrorResponse.statusCode);
                vertxContext.end(this.objectMapper.writeValueAsString(httpErrorResponse.errorMessageValue));
            } catch (final JsonProcessingException e) {
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
                    } catch (final RuntimeException e) {
                        handleException(vertxContext, e);
                    }
                } else {
                    vertxContext.put("state", s);
                    vertxContext.next();
                }
            });
    }

    private void handleException(final RequestContext vertxContext, final Exception e) {
        exceptionHandler.accept(e);
        vertxContext.setStatusCode(500);
        vertxContext.end("{\"message\":\"Something went wrong\"}");
    }
}
