package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kiw.template.web.test.handler.RouteConfig;

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

    public void handle(MapInstruction applicationInstruction, VertxContext vertxContext, Object applicationState) {
        HttpContext httpContext = new HttpContext(vertxContext);

        HttpResult result;
        try {
            result = applicationInstruction.handle(vertxContext.get("state"), httpContext, applicationState);
        } catch (Exception e) {
            handleException(vertxContext, e);
            return;
        }

        if (result.isSuccessful()) {
            if (applicationInstruction.lastStep) {
                try {
                    vertxContext.end(this.objectMapper.writeValueAsString(result.successValue));
                } catch (JsonProcessingException e) {
                    handleException(vertxContext, e);
                }
            } else {
                vertxContext.put("state", result.successValue);
                vertxContext.next();
            }
        } else {
            try {
                vertxContext.setStatusCode(result.statusCode);
                vertxContext.end(this.objectMapper.writeValueAsString(result.errorMessageValue));
            } catch (JsonProcessingException e) {
                handleException(vertxContext, e);
            }
        }
    }

    private void handleException(VertxContext vertxContext, Exception e) {
        exceptionHandler.accept(e);
        vertxContext.setStatusCode(500);
        vertxContext.end("{\"message\":\"Something went wrong\"}");
    }
}
