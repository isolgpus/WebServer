package io.kiw.web;

import io.kiw.web.internal.RoutesRegistrar;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.function.BiConsumer;

public class VertxWebServer<APP> implements WebServer<APP> {
    private final Vertx vertx;
    private final APP applicationState;

    public VertxWebServer(Vertx vertx, APP applicationState) {
        this.vertx = vertx;
        this.applicationState = applicationState;
    }

    public static <APP> WebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        return start(routesRegisterConsumer, new WebServiceConfigBuilder().build());
    }

    public static <APP> WebServer<APP> start(ApplicationRoutesRegister<APP> routesRegisterConsumer, WebServerConfig webServerConfig) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        APP applicationState = RoutesRegistrar.register(router, vertx, routesRegisterConsumer, webServerConfig.defaultTimeoutMillis, webServerConfig.exceptionHandler, webServerConfig.maxBodySize, webServerConfig.corsConfig);

        httpServer.requestHandler(router).listen(webServerConfig.port).toCompletionStage().toCompletableFuture().join();
        return new VertxWebServer<>(vertx, applicationState);
    }

    @Override
    public <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer) {
        vertx.runOnContext((v) -> applicationStateConsumer.accept(immutableState, applicationState));
    }

    @Override
    public void stop() {
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }
}
