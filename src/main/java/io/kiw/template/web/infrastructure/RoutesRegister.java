package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RoutesRegister(RouterWrapper router) {

        this.router = router;
    }

    public <T extends JsonRequest, R extends JsonResponse> void registerJsonRoute(String path, Method method, VertxJsonRoute<T, R> vertxJsonRoute) {

        HttpControlStream<T> httpControlStream = new HttpControlStream<>(new ArrayList<>());
        httpControlStream.flatMap((request, ctx) -> {
            ctx.addResponseHeader("Content-Type", "application/json");

            if (method.canHaveABody() && ctx.ctx.getRequestBody() == null) {
                return HttpResult.error(400, new MessageResponse("Invalid request"));
            }

            try
            {
                T jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.ctx.getRequestBody(), vertxJsonRoute) : null;
                return HttpResult.success(jsonRequest);
            }
            catch (JsonProcessingException e) {
                return HttpResult.error(500, new MessageResponse("something went wrong"));
            }
        });

        Flow flow = vertxJsonRoute.handle(httpControlStream);


        router.route(path, method, "*/json", "application/json", flow);


    }

}
