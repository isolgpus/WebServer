package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import static io.kiw.template.web.infrastructure.Method.POST;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RoutesRegister(RouterWrapper router) {

        this.router = router;
    }

    public <IN extends JsonRequest, OUT extends JsonResponse, APP> void registerJsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute) {

        HttpControlStream<IN, APP> httpControlStream = new HttpControlStream<>(new ArrayList<>(), true, applicationState);
        httpControlStream.flatMap((request, ctx, as) -> {
            ctx.addResponseHeader("Content-Type", "application/json");

            if (method.canHaveABody() && ctx.ctx.getRequestBody() == null) {
                return HttpResult.error(400, new MessageResponse("Invalid request"));
            }

            try
            {
                IN jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.ctx.getRequestBody(), vertxJsonRoute) : null;
                return HttpResult.success(jsonRequest);
            }
            catch (JsonProcessingException e) {
                return HttpResult.error(500, new MessageResponse("something went wrong"));
            }
        });

        Flow flow = vertxJsonRoute.handle(httpControlStream);


        router.route(path, method, "*/json", "application/json", flow);


    }

    public <APP> void registerJsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter) {
        HttpControlStream<Void, APP> objectAPPHttpControlStream = new HttpControlStream<>(new ArrayList<>(), false, applicationState);
        Flow flow = jsonFilter.handle(objectAPPHttpControlStream);


        router.route(path, POST, "*/json", "application/json", flow);
    }
}
