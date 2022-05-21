package io.kiw.template.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kiw.template.web.test.handler.RouteConfig;
import io.kiw.template.web.test.handler.RouteConfigBuilder;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Map;

import static io.kiw.template.web.infrastructure.Method.POST;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RoutesRegister(RouterWrapper router) {

        this.router = router;
    }

    public <IN extends JsonRequest, OUT extends JsonResponse, APP> void jsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute) {
        jsonRoute(path,method, applicationState, vertxJsonRoute, new RouteConfigBuilder().build());
    }
    public <IN extends JsonRequest, OUT extends JsonResponse, APP> void jsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute, RouteConfig routeConfig) {

        HttpControlStream<IN, APP> httpControlStream = new HttpControlStream<>(new ArrayList<>(), true, applicationState);
        httpControlStream.flatMap((request, ctx, as) -> {
            ctx.addResponseHeader("Content-Type", "application/json");

            if (method.canHaveABody() && ctx.ctx.getRequestBody() == null) {
                return HttpResult.error(400, new MessageResponse("Invalid json request"));
            }

            try
            {
                IN jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.ctx.getRequestBody(), vertxJsonRoute) : null;
                return HttpResult.success(jsonRequest);
            }
            catch (JsonProcessingException e) {
                return HttpResult.error(400, new MessageResponse("Invalid json request"));
            }
        });

        Flow flow = vertxJsonRoute.handle(httpControlStream);


        router.route(path, method, "*/json", "application/json", flow, routeConfig);


    }

    public <APP> void jsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter) {
       jsonFilter(path, applicationState, jsonFilter, new RouteConfigBuilder().build());
    }

    public <APP> void jsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter, RouteConfig routeConfig) {
        HttpControlStream<Void, APP> objectAPPHttpControlStream = new HttpControlStream<>(new ArrayList<>(), false, applicationState);
        Flow flow = jsonFilter.handle(objectAPPHttpControlStream);


        router.route(path, POST, "*/json", "application/json", flow, routeConfig);
    }

    public  <OUT extends JsonResponse, APP>  void uploadFile(String path, Method method, APP applicationState, VertxFileUploadRoute<OUT, APP> fileUploaderHandler) {

        HttpControlStream<Map<String, Buffer>, APP> httpControlStream = new HttpControlStream<>(new ArrayList<>(), true, applicationState);

        HttpControlStream<Map<String, Buffer>, APP> fileUploadStream = httpControlStream.blockingFlatMap((request, ctx) -> {
            ctx.addResponseHeader("Content-Type", "application/json");

            Map<String, Buffer> uploadedFile = ctx.resolveUploadedFiles();
            return HttpResult.success(uploadedFile);
        });
        Flow flow = fileUploaderHandler.handle(fileUploadStream);

        router.route(path, method, "multipart/form-data", "application/json", flow, new RouteConfigBuilder().build());
    }
}
