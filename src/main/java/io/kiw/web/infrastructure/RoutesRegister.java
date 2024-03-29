package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kiw.web.infrastructure.ender.FileEnder;
import io.kiw.web.infrastructure.ender.JsonEnder;
import io.kiw.web.test.VertxFileDownloadRoute;
import io.kiw.web.test.handler.RouteConfig;
import io.kiw.web.test.handler.RouteConfigBuilder;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.Map;

import static io.kiw.web.infrastructure.Method.POST;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RoutesRegister(RouterWrapper router) {

        this.router = router;
    }

    public <IN, OUT, APP> void jsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute) {
        jsonRoute(path, method, applicationState, vertxJsonRoute, new RouteConfigBuilder().build());
    }


    public <IN, OUT, APP> void jsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute, RouteConfig routeConfig) {

        HttpResponseStream<IN, APP> httpResponseStream = new HttpResponseStream<>(new ArrayList<>(), true, applicationState, new JsonEnder(objectMapper))
            .flatMap((request, ctx, as) -> {
                ctx.addResponseHeader("Content-Type", "application/json");

                if (method.canHaveABody() && ctx.ctx.getRequestBody() == null) {
                    return HttpResult.error(400, new ErrorMessageResponse("Invalid json request"));
                }

                try {
                    IN jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.ctx.getRequestBody(), vertxJsonRoute) : null;
                    return HttpResult.success(jsonRequest);
                } catch (JsonProcessingException e) {
                    return HttpResult.error(400, new ErrorMessageResponse("Invalid json request"));
                }
            }).flatMap(((request, httpContext, as) -> HttpResult.success(request)));

        Flow<OUT> flow = vertxJsonRoute.handle(httpResponseStream);


        router.route(path, method, "*", "application/json", flow, routeConfig);
    }

    public <APP> void jsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter) {
       jsonFilter(path, applicationState, jsonFilter, new RouteConfigBuilder().build());
    }

    public <APP> void jsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter, RouteConfig routeConfig) {
        HttpResponseStream<Void, APP> objectAPPHttpResponseStream = new HttpResponseStream<>(new ArrayList<>(), false, applicationState, null);
        Flow flow = jsonFilter.handle(objectAPPHttpResponseStream);


        router.route(path, POST, "*/json", "application/json", flow, routeConfig);
    }

    public  <OUT, APP>  void uploadFileRoute(String path, Method method, APP applicationState, VertxFileUploadRoute<OUT, APP> fileUploaderHandler) {

        HttpResponseStream<Map<String, Buffer>, APP> httpResponseStream = new HttpResponseStream<>(new ArrayList<>(), true, applicationState,  new JsonEnder(objectMapper));

        HttpResponseStream<Map<String, Buffer>, APP> fileUploadStream = httpResponseStream.flatMap((request, ctx, app) -> {
            ctx.addResponseHeader("Content-Type", "application/json");

            Map<String, Buffer> uploadedFile = ctx.resolveUploadedFiles();
            return HttpResult.success(uploadedFile);
        });
        Flow flow = fileUploaderHandler.handle(fileUploadStream);

        router.route(path, method, "multipart/form-data", "application/json", flow, new RouteConfigBuilder().build());
    }

    public <IN, APP> void downloadFileRoute(String path, Method method, APP applicationState, VertxFileDownloadRoute<IN, APP> fileDownloadHandler, String contentType) {
        HttpResponseStream<IN, APP> httpResponseStream = new HttpResponseStream<>(new ArrayList<>(), true, applicationState, new FileEnder());

        HttpResponseStream<IN, APP> fileDownloadStream = httpResponseStream.flatMap((request, ctx, app) -> {
            ctx.addResponseHeader("Content-Type", contentType);
            ctx.addResponseHeader(TRANSFER_ENCODING, "chunked");
            return HttpResult.success(request);
        });
        Flow flow = fileDownloadHandler.handle(fileDownloadStream);

        router.route(path, method, "*", contentType, flow, new RouteConfigBuilder().build());
    }
}
