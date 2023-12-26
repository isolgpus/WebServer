package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kiw.web.infrastructure.ender.Ender;
import io.kiw.web.test.handler.RouteConfig;
import io.kiw.result.Result;

import java.util.function.Consumer;

public abstract class RouterWrapper {

    private final Consumer<Exception> exceptionHandler;

    public RouterWrapper(Consumer<Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected abstract void route(String path, Method method, String consumes, String provides, Flow flow, RouteConfig routeConfig);

     public <T> void handle(MapInstruction<Object, T, Object> applicationInstruction, VertxContext vertxContext, Object applicationState, Ender ender) {
         HttpContext httpContext = new HttpContext(vertxContext);
         Result<HttpErrorResponse, T> result;
         try {
             result = applicationInstruction.handle(vertxContext.get("state"), httpContext, applicationState);
         } catch (Exception e) {
             handleException(vertxContext, e);
             return;
         }

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
                    try
                    {
                        ender.end(vertxContext, s);
                    }
                    catch (RuntimeException e) {
                        handleException(vertxContext, e);
                    }
                } else {
                    vertxContext.put("state", s);
                    vertxContext.next();
                }
            });
    }

    private void handleException(VertxContext vertxContext, Exception e) {
        exceptionHandler.accept(e);
        vertxContext.setStatusCode(500);
        vertxContext.end("{\"message\":\"Something went wrong\"}");
    }
}
