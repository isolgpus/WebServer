package io.kiw.web.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kiw.web.infrastructure.cors.CorsConfig;
import io.kiw.web.infrastructure.ender.FileEnder;
import io.kiw.web.infrastructure.ender.JsonEnder;
import io.kiw.web.infrastructure.openapi.OpenApiCollector;
import io.kiw.web.infrastructure.openapi.OpenApiRoute;
import io.kiw.web.infrastructure.openapi.RouteDescriptor;
import io.kiw.web.infrastructure.openapi.TypeResolver;
import io.kiw.web.test.handler.RouteConfig;
import io.kiw.web.test.handler.RouteConfigBuilder;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final OpenApiCollector openApiCollector = new OpenApiCollector();

    public RoutesRegister(RouterWrapper router) {

        this.router = router;
    }

    public OpenApiCollector getOpenApiCollector() {
        return openApiCollector;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void cors(CorsConfig corsConfig) {
        router.configureCors(corsConfig);
    }

    public <IN, OUT, APP> void jsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute) {
        jsonRoute(path, method, applicationState, vertxJsonRoute, new RouteConfigBuilder().build());
    }


    public <IN, OUT, APP> void jsonRoute(String path, Method method, APP applicationState, VertxJsonRoute<IN, OUT, APP> vertxJsonRoute, RouteConfig routeConfig) {

        HttpStream<IN, APP> httpStream = new HttpStream<>(new ArrayList<>(), true, applicationState, new JsonEnder(objectMapper))
            .flatMap(ctx -> {
                ctx.http().addResponseHeader("Content-Type", "application/json");

                if (method.canHaveABody() && ctx.http().ctx.getRequestBody() == null) {
                    return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Invalid json request"));
                }

                try {
                    IN jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.http().ctx.getRequestBody(), vertxJsonRoute) : null;
                    return HttpResult.success(jsonRequest);
                } catch (JsonProcessingException e) {
                    return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Invalid json request"));
                }
            }).flatMap(ctx -> HttpResult.success(ctx.in()));

        RequestPipeline<OUT> flow = vertxJsonRoute.handle(httpStream);

        Type[] typeArgs = TypeResolver.resolveTypeArguments(vertxJsonRoute.getClass(), VertxJsonRoute.class);
        openApiCollector.addRoute(new RouteDescriptor(
            path, method,
            typeArgs != null ? typeArgs[0] : null,
            typeArgs != null ? typeArgs[1] : null,
            "application/json", "application/json",
            RouteDescriptor.RouteKind.JSON,
            routeConfig.openApiMetadata.orElse(null)
        ));

        router.route(path, method, "*", "application/json", flow, routeConfig);
    }

    public <APP> void jsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter) {
       jsonFilter(path, applicationState, jsonFilter, new RouteConfigBuilder().build());
    }

    public <APP> void jsonFilter(final String path, APP applicationState, VertxJsonFilter jsonFilter, RouteConfig routeConfig) {
        HttpStream<Void, APP> objectAPPHttpStream = new HttpStream<>(new ArrayList<>(), false, applicationState, null);
        RequestPipeline flow = jsonFilter.handle(objectAPPHttpStream);


        router.route(path, "*", "application/json", flow, routeConfig);
    }

    public  <OUT, APP>  void uploadFileRoute(String path, Method method, APP applicationState, VertxFileUploadRoute<OUT, APP> fileUploaderHandler) {

        HttpStream<Map<String, Buffer>, APP> httpStream = new HttpStream<>(new ArrayList<>(), true, applicationState,  new JsonEnder(objectMapper));

        HttpStream<Map<String, Buffer>, APP> fileUploadStream = httpStream.flatMap(ctx -> {
            ctx.http().addResponseHeader("Content-Type", "application/json");

            Map<String, Buffer> uploadedFile = ctx.http().resolveUploadedFiles();
            return HttpResult.success(uploadedFile);
        });
        RequestPipeline flow = fileUploaderHandler.handle(fileUploadStream);

        Type[] typeArgs = TypeResolver.resolveTypeArguments(fileUploaderHandler.getClass(), VertxFileUploadRoute.class);
        openApiCollector.addRoute(new RouteDescriptor(
            path, method,
            null,
            typeArgs != null ? typeArgs[0] : null,
            "multipart/form-data", "application/json",
            RouteDescriptor.RouteKind.UPLOAD,
            null
        ));

        router.route(path, method, "multipart/form-data", "application/json", flow, new RouteConfigBuilder().build());
    }

    public <IN, OUT, APP> void webSocketRoute(String path, APP applicationState, WebSocketRoute<IN, OUT, APP> webSocketRoute) {
        Type[] typeArgs = TypeResolver.resolveTypeArguments(webSocketRoute.getClass(), WebSocketRoute.class);
        openApiCollector.addRoute(new RouteDescriptor(
            path, null,
            typeArgs != null ? typeArgs[0] : null,
            typeArgs != null ? typeArgs[1] : null,
            null, null,
            RouteDescriptor.RouteKind.WEBSOCKET,
            null
        ));

        WebSocketRouteHandler<IN, OUT, APP> handler = new WebSocketRouteHandler<>(webSocketRoute, objectMapper, applicationState, router.getExceptionHandler());
        router.webSocketRoute(path, handler);
    }

    public <IN, APP> void downloadFileRoute(String path, Method method, APP applicationState, VertxFileDownloadRoute<IN, APP> fileDownloadHandler, String contentType) {
        HttpStream<IN, APP> httpStream = new HttpStream<>(new ArrayList<>(), true, applicationState, new FileEnder());

        HttpStream<IN, APP> fileDownloadStream = httpStream.flatMap(ctx -> {
            ctx.http().addResponseHeader("Content-Type", contentType);
            ctx.http().addResponseHeader(TRANSFER_ENCODING, "chunked");
            return HttpResult.success(ctx.in());
        });
        RequestPipeline flow = fileDownloadHandler.handle(fileDownloadStream);

        Type[] typeArgs = TypeResolver.resolveTypeArguments(fileDownloadHandler.getClass(), VertxFileDownloadRoute.class);
        openApiCollector.addRoute(new RouteDescriptor(
            path, method,
            typeArgs != null ? typeArgs[0] : null,
            DownloadFileResponse.class,
            "*", contentType,
            RouteDescriptor.RouteKind.DOWNLOAD,
            null
        ));

        router.route(path, method, "*", contentType, flow, new RouteConfigBuilder().build());
    }

    public void serveOpenApiSpec(String path, String title, String version, String description) {
        OpenApiRoute openApiRoute = new OpenApiRoute(openApiCollector, objectMapper, title, version, description);
        RouteConfig config = new RouteConfigBuilder().openApi().hidden().build();
        jsonRoute(path, Method.GET, null, openApiRoute, config);
    }
}
