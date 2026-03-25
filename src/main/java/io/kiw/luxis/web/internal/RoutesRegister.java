package io.kiw.luxis.web.internal;

import tools.jackson.databind.ObjectMapper;
import io.kiw.luxis.web.cors.CorsConfig;
import io.kiw.luxis.web.handler.VertxFileDownloadRoute;
import io.kiw.luxis.web.handler.VertxFileUploadRoute;
import io.kiw.luxis.web.handler.VertxJsonFilter;
import io.kiw.luxis.web.handler.VertxJsonRoute;
import io.kiw.luxis.web.handler.WebSocketRoute;
import io.kiw.luxis.web.http.DownloadFileResponse;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.http.ErrorStatusCode;
import io.kiw.luxis.web.http.HttpBuffer;
import io.kiw.luxis.web.http.HttpResult;
import io.kiw.luxis.web.http.Method;
import io.kiw.luxis.web.internal.ender.FileEnder;
import io.kiw.luxis.web.internal.ender.JsonEnder;
import io.kiw.luxis.web.openapi.OpenApiCollector;
import io.kiw.luxis.web.openapi.OpenApiRoute;
import io.kiw.luxis.web.openapi.RouteDescriptor;
import io.kiw.luxis.web.openapi.TypeResolver;
import io.kiw.luxis.web.pipeline.HttpMapStream;
import io.kiw.luxis.web.pipeline.HttpStream;
import io.kiw.luxis.web.RouteConfig;
import io.kiw.luxis.web.RouteConfigBuilder;
import io.kiw.luxis.web.WebSocketRouteConfig;
import io.kiw.luxis.web.WebSocketRouteConfigBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;

public class RoutesRegister {

    private final RouterWrapper router;
    private final ExecutionDispatcher executionDispatcher;
    private final ObjectMapper objectMapper = JacksonUtil.createMapper();
    private final OpenApiCollector openApiCollector = new OpenApiCollector();
    private final PendingAsyncResponses pendingAsyncResponses;

    public RoutesRegister(final RouterWrapper router, final ExecutionDispatcher executionDispatcher, final PendingAsyncResponses pendingAsyncResponses) {
        this.router = router;
        this.executionDispatcher = executionDispatcher;
        this.pendingAsyncResponses = pendingAsyncResponses;
    }

    public OpenApiCollector getOpenApiCollector() {
        return openApiCollector;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void cors(final CorsConfig corsConfig) {
        router.configureCors(corsConfig);
    }

    public <IN, OUT, APP> void jsonRoute(final String path, final Method method, final APP applicationState, final VertxJsonRoute<IN, OUT, APP> vertxJsonRoute) {
        jsonRoute(path, method, applicationState, vertxJsonRoute, new RouteConfigBuilder().build());
    }


    public <IN, OUT, APP> void jsonRoute(final String path, final Method method, final APP applicationState, final VertxJsonRoute<IN, OUT, APP> vertxJsonRoute, final RouteConfig routeConfig) {

        final ArrayList<MapInstruction> chain = new ArrayList<>();
        new HttpMapStream<>(chain, true, applicationState, new JsonEnder(objectMapper), pendingAsyncResponses)
            .flatMap(ctx -> {
                ctx.http().addResponseHeader("Content-Type", "application/json");

                if (method.canHaveABody() && ctx.http().ctx.getRequestBody() == null) {
                    return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Invalid json request"));
                }

                try {
                    final IN jsonRequest = method.canHaveABody() ? objectMapper.readValue(ctx.http().ctx.getRequestBody(), vertxJsonRoute) : null;
                    return HttpResult.success(jsonRequest);
                } catch (final Exception e) {
                    return HttpResult.error(ErrorStatusCode.BAD_REQUEST, new ErrorMessageResponse("Invalid json request"));
                }
            }).flatMap(ctx -> HttpResult.success(ctx.in()));

        final HttpStream<IN, APP> httpStream = new HttpStream<>(chain, true, applicationState, new JsonEnder(objectMapper), pendingAsyncResponses);
        final RequestPipeline flow = vertxJsonRoute.handle(httpStream);

        final Type[] typeArgs = TypeResolver.resolveTypeArguments(vertxJsonRoute.getClass(), VertxJsonRoute.class);
        openApiCollector.addRoute(new RouteDescriptor(
            path, method,
            typeArgs != null ? typeArgs[0] : null,
            typeArgs != null ? typeArgs[1] : null,
            "application/json", "application/json",
            RouteDescriptor.RouteKind.JSON,
            routeConfig.openApiMetadata().orElse(null)
        ));

        router.route(path, method, "*", "application/json", flow, routeConfig);
    }

    public <APP> void jsonFilter(final String path, final APP applicationState, final VertxJsonFilter<APP> jsonFilter) {
        jsonFilter(path, applicationState, jsonFilter, new RouteConfigBuilder().build());
    }

    public <APP> void jsonFilter(final String path, final APP applicationState, final VertxJsonFilter<APP> jsonFilter, final RouteConfig routeConfig) {
        final ArrayList<MapInstruction> chain = new ArrayList<>();
        new HttpMapStream<>(chain, false, applicationState, null, pendingAsyncResponses)
            .map(ctx -> {
                ctx.http().addResponseHeader("Content-Type", "application/json");
                return null;
            });
        final HttpStream<Void, APP> httpStream = new HttpStream<>(chain, false, applicationState, null, pendingAsyncResponses);
        final RequestPipeline<Void> flow = jsonFilter.handle(httpStream);


        router.route(path, "*", "application/json", flow, routeConfig);
    }

    public <OUT, APP> void uploadFileRoute(final String path, final Method method, final APP applicationState, final VertxFileUploadRoute<OUT, APP> fileUploaderHandler) {

        final HttpMapStream<Map<String, HttpBuffer>, APP> httpStream = new HttpMapStream<>(new ArrayList<>(), true, applicationState, new JsonEnder(objectMapper), pendingAsyncResponses);

        final HttpMapStream<Map<String, HttpBuffer>, APP> fileUploadStream = httpStream.flatMap(ctx -> {
            ctx.http().addResponseHeader("Content-Type", "application/json");

            final Map<String, HttpBuffer> uploadedFile = ctx.http().resolveUploadedFiles();
            return HttpResult.success(uploadedFile);
        });
        final RequestPipeline<OUT> flow = fileUploaderHandler.handle(fileUploadStream);

        final Type[] typeArgs = TypeResolver.resolveTypeArguments(fileUploaderHandler.getClass(), VertxFileUploadRoute.class);
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

    public <IN, OUT, APP> void webSocketRoute(final String path, final APP applicationState, final WebSocketRoute<IN, OUT, APP> webSocketRoute) {
        webSocketRoute(path, applicationState, webSocketRoute, new WebSocketRouteConfigBuilder().build());
    }

    public <IN, OUT, APP> void webSocketRoute(final String path, final APP applicationState, final WebSocketRoute<IN, OUT, APP> webSocketRoute, final WebSocketRouteConfig config) {
        final Type[] typeArgs = TypeResolver.resolveTypeArguments(webSocketRoute.getClass(), WebSocketRoute.class);
        openApiCollector.addRoute(new RouteDescriptor(
            path, null,
            typeArgs != null ? typeArgs[0] : null,
            typeArgs != null ? typeArgs[1] : null,
            null, null,
            RouteDescriptor.RouteKind.WEBSOCKET,
            null
        ));

        final WebSocketRouteHandler<IN, OUT, APP> handler = new WebSocketRouteHandler<>(webSocketRoute, objectMapper, applicationState, router.getExceptionHandler(), executionDispatcher, config);
        router.webSocketRoute(path, handler);
    }

    public <IN, APP> void downloadFileRoute(final String path, final Method method, final APP applicationState, final VertxFileDownloadRoute<IN, APP> fileDownloadHandler, final String contentType) {
        final HttpMapStream<IN, APP> httpStream = new HttpMapStream<>(new ArrayList<>(), true, applicationState, new FileEnder(), pendingAsyncResponses);

        final HttpMapStream<IN, APP> fileDownloadStream = httpStream.flatMap(ctx -> {
            ctx.http().addResponseHeader("Content-Type", contentType);
            ctx.http().addResponseHeader(TRANSFER_ENCODING, "chunked");
            return HttpResult.success(ctx.in());
        });
        final RequestPipeline flow = fileDownloadHandler.handle(fileDownloadStream);

        final Type[] typeArgs = TypeResolver.resolveTypeArguments(fileDownloadHandler.getClass(), VertxFileDownloadRoute.class);
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

    public void serveOpenApiSpec(final String path, final String title, final String version, final String description) {
        final OpenApiRoute openApiRoute = new OpenApiRoute(openApiCollector, objectMapper, title, version, description);
        final RouteConfig config = new RouteConfigBuilder().openApi().hidden().build();
        jsonRoute(path, Method.GET, null, openApiRoute, config);
    }
}
