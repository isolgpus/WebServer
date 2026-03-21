package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.handler.*;
import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.validation.*;
import io.kiw.luxis.web.websocket.*;

import io.kiw.luxis.web.ApplicationRoutesRegister;
import io.kiw.luxis.web.cors.CorsConfig;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;

public class VertxRoutesRegistrar {

    public static <R> R register(Router router, Vertx vertx, ApplicationRoutesRegister<R> routesRegisterConsumer, int defaultTimeoutMillis, Consumer<Exception> exceptionHandler, OptionalLong maxBodySize, Optional<CorsConfig> corsConfig) {
        VertxRouterWrapperImpl routerWrapper = new VertxRouterWrapperImpl(router, defaultTimeoutMillis, exceptionHandler);
        corsConfig.ifPresent(routerWrapper::configureCors);

        BodyHandler handler = BodyHandler.create();
        maxBodySize.ifPresent(handler::setBodyLimit);
        router.route().handler(handler);

        RoutesRegister routesRegister = new RoutesRegister(routerWrapper, new VertxWebSocketRouterWrapperImpl(vertx));
        return routesRegisterConsumer.registerRoutes(routesRegister);

    }

}
