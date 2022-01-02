package io.kiw.template.web;

import io.kiw.template.web.infrastructure.RoutesRegistrar;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.util.function.BiConsumer;

public class WebServer<APP> {
    private final Vertx vertx;
    private final APP applicationState;

    public WebServer(Vertx vertx, APP applicationState) {

        this.vertx = vertx;
        this.applicationState = applicationState;
    }

    public static <APP> WebServer<APP> start(int portNumber, ApplicationRoutesRegister<APP> routesRegisterConsumer) {
        Vertx vertx = Vertx.vertx();
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);

        APP applicationState = RoutesRegistrar.register(router, routesRegisterConsumer);

        httpServer.requestHandler(router).listen(portNumber);
        return new WebServer<>(vertx, applicationState);
    }

    public <IN> void apply(IN immutableState, BiConsumer<IN, APP> applicationStateConsumer) {
        vertx.runOnContext((v) -> applicationStateConsumer.accept(immutableState, applicationState));
    }
}
