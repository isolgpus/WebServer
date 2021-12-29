package io.kiw.template.web;

import io.kiw.template.web.infrastructure.RoutesRegister;
import io.kiw.template.web.infrastructure.RoutesRegistrar;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.function.Consumer;

public class WebServer {
    public static void start(int portNumber, Consumer<RoutesRegister> routesRegisterConsumer) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        RoutesRegistrar.register(router, routesRegisterConsumer);

        httpServer.requestHandler(router).listen(portNumber);
    }
}
