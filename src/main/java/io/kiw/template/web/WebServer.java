package io.kiw.template.web;

import io.kiw.template.web.infrastructure.RoutesRegistrar;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.function.Consumer;

public class WebServer<T> {
    private final Vertx vertx;
    private final T applicationState;

    public WebServer(Vertx vertx, T applicationState) {

        this.vertx = vertx;
        this.applicationState = applicationState;
    }

    public static <T> WebServer<T> start(int portNumber, ApplicationRoutesRegister<T> routesRegisterConsumer) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        T applicationState = RoutesRegistrar.register(router, routesRegisterConsumer);

        httpServer.requestHandler(router).listen(portNumber);
        return new WebServer<>(vertx, applicationState);
    }

    public void run(Consumer<T> applicationStateConsumer) {
        vertx.runOnContext((v) -> applicationStateConsumer.accept(applicationState));
    }
}
