package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RoutesRegister(RouterWrapper router) {

        this.router = router;
    }

    public <T extends JsonRequest> void registerJsonRoute(String path, Method method, VertxJsonRoute<T> vertxJsonRoute) {
        router.route(path, method, "*/json", "application/json", ctx -> {
            ctx.addResponseHeader("Content-Type", "application/json");

            try {
                if(method.canHaveABody() && ctx.getRequestBody() == null)
                {
                    ctx.setStatusCode(400);
                    ctx.end(objectMapper.writeValueAsString(new MessageResponse("Invalid request")));
                    return;
                }

                T jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.getRequestBody(), vertxJsonRoute) : null;
                Object response = vertxJsonRoute.handle(jsonRequest, new HttpContext(ctx));
                ctx.end(objectMapper.writeValueAsString(response));
            }
            catch (JsonProcessingException e)
            {
                ctx.setStatusCode(500);
                ctx.end("{'message':'Something went wrong'}");
            }
        });

    }

}
