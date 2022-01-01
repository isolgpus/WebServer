package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class RouterWrapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract void route(String path, Method method, String consumes, String provides, Flow flow);

    public void handle(MapInstruction applicationInstruction, VertxContext vertxContext, Object applicationState) {
        HttpContext httpContext = new HttpContext(vertxContext);
        HttpResult result = applicationInstruction.consumer.handle(vertxContext.get("state"), httpContext, applicationState);

        if (result.isSuccessful()) {
            if (applicationInstruction.lastStep) {
                try {
                    vertxContext.end(this.objectMapper.writeValueAsString(result.successValue));
                } catch (JsonProcessingException e) {
                    vertxContext.setStatusCode(500);
                    vertxContext.end("{\"message\": \"Something went wrong\"}");
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
                vertxContext.setStatusCode(500);
                vertxContext.end("{\"message\": \"Something went wrong\"}");
            }
        }
    }
}
